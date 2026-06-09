package br.com.fiap.chronos.controller;

import br.com.fiap.chronos.dto.StatusUpdateRequest;
import br.com.fiap.chronos.dto.TransactionRequest;
import br.com.fiap.chronos.dto.TransactionResponse;
import br.com.fiap.chronos.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST que expõe os endpoints para gerenciar o ciclo de vida
 * das transações financeiras cislunares no buffer DTN.
 *
 * <p>Delega toda a lógica de negócio para {@link TransactionService},
 * limitando-se ao mapeamento HTTP e à montagem dos links HATEOAS.</p>
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transações DTN", description = "Endpoints para gerenciamento do buffer de transações financeiras cislunar")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Construtor com injeção da camada de serviço via Spring IoC.
     *
     * @param transactionService serviço de regras de negócio das transações DTN
     */
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Recebe uma transação, persiste no buffer DTN e executa a procedure de correção
     * relativística do timestamp lunar.
     */
    @Operation(
            summary = "Sincronizar transação cislunar",
            description = "Registra uma nova transação no buffer DTN, executa a stored procedure " +
                    "SP_CORRIGIR_TEMPO_LUNAR para correção relativística e retorna o estado final com links HATEOAS."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transação sincronizada com sucesso",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "422", description = "Falha na validação dos campos obrigatórios"),
            @ApiResponse(responseCode = "500", description = "Erro interno na execução da procedure")
    })
    @PostMapping("/sync")
    public ResponseEntity<EntityModel<TransactionResponse>> syncTransaction(
            @Valid @RequestBody TransactionRequest request) {

        TransactionResponse response = transactionService.sincronizarTransacao(request);

        EntityModel<TransactionResponse> model = EntityModel.of(response);
        model.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TransactionController.class)
                .getTransactionById(response.idTx())).withSelfRel());
        model.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TransactionController.class)
                .getBufferTransactions(null)).withRel("buffer"));

        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    /**
     * Busca uma transação específica pelo seu identificador único.
     */
    @Operation(
            summary = "Buscar transação por ID",
            description = "Recupera os dados completos de uma transação DTN pelo seu identificador. " +
                    "Retorna 404 se a transação não existir."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transação encontrada",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<TransactionResponse>> getTransactionById(
            @Parameter(description = "Identificador único da transação DTN", example = "1")
            @PathVariable Long id) {

        TransactionResponse response = transactionService.buscarPorId(id);

        EntityModel<TransactionResponse> model = EntityModel.of(response);
        model.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TransactionController.class)
                .getTransactionById(id)).withSelfRel());
        model.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TransactionController.class)
                .getBufferTransactions(null)).withRel("buffer"));

        return ResponseEntity.ok(model);
    }

    /**
     * Lista todas as transações cadastradas no buffer, com filtro opcional por status.
     */
    @Operation(
            summary = "Listar transações do buffer",
            description = "Retorna todas as transações DTN armazenadas no buffer. " +
                    "Use o parâmetro opcional 'status' para filtrar por PENDING, SYNCED ou CANCELLED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<EntityModel<TransactionResponse>>> getBufferTransactions(
            @Parameter(description = "Filtro de status: PENDING | SYNCED | CANCELLED")
            @RequestParam(required = false) String status) {

        List<EntityModel<TransactionResponse>> models = transactionService.listarTransacoes(status)
                .stream()
                .map(tx -> {
                    EntityModel<TransactionResponse> model = EntityModel.of(tx);
                    model.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TransactionController.class)
                            .getTransactionById(tx.idTx())).withSelfRel());
                    return model;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(models);
    }

    /**
     * Atualiza parcialmente o status de sincronização de uma transação existente.
     */
    @Operation(
            summary = "Atualizar status da transação (PATCH)",
            description = "Evolui o status de sincronização de uma transação DTN. " +
                    "Transições válidas: PENDING → SYNCED ou PENDING → CANCELLED. " +
                    "Status terminais (SYNCED, CANCELLED) não podem ser alterados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Transição de status inválida ou status terminal"),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada"),
            @ApiResponse(responseCode = "422", description = "Valor de status inválido")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<EntityModel<TransactionResponse>> updateTransactionStatus(
            @Parameter(description = "Identificador único da transação DTN", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {

        TransactionResponse response = transactionService.atualizarStatus(id, request);

        EntityModel<TransactionResponse> model = EntityModel.of(response);
        model.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TransactionController.class)
                .getTransactionById(id)).withSelfRel());

        return ResponseEntity.ok(model);
    }

    /**
     * Remove permanentemente uma transação do buffer DTN.
     */
    @Operation(
            summary = "Excluir transação do buffer",
            description = "Remove permanentemente uma transação DTN pelo seu identificador. Retorna 404 se não existir."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Transação removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "Identificador único da transação DTN", example = "1")
            @PathVariable Long id) {

        transactionService.deletarTransacao(id);
        return ResponseEntity.noContent().build();
    }
}
