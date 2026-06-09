package br.com.fiap.chronos.service;

import br.com.fiap.chronos.dto.StatusUpdateRequest;
import br.com.fiap.chronos.dto.TransactionRequest;
import br.com.fiap.chronos.dto.TransactionResponse;
import br.com.fiap.chronos.exception.ProcedureExecutionException;
import br.com.fiap.chronos.exception.TransactionNotFoundException;
import br.com.fiap.chronos.model.CislunarNodePair;
import br.com.fiap.chronos.model.TransactionBuffer;
import br.com.fiap.chronos.repository.TransactionBufferRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Camada de serviço responsável pela lógica de negócio das transações DTN.
 *
 * <p>Centraliza as regras de domínio, desacoplando o {@code TransactionController}
 * da lógica de persistência e execução de procedures, em conformidade com o
 * princípio de responsabilidade única (SRP).</p>
 */
@Service
public class TransactionService {

    private final TransactionBufferRepository repository;
    private final EntityManager entityManager;

    /**
     * Construtor com injeção de dependência do repositório JPA e do EntityManager.
     *
     * @param repository    repositório de transações do buffer DTN
     * @param entityManager gerenciador de entidade JPA para chamada de procedures
     */
    public TransactionService(TransactionBufferRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    /**
     * Sincroniza uma nova transação cislunar: persiste no buffer, executa a procedure
     * de correção relativística e retorna o DTO de resposta com o estado final.
     *
     * @param request DTO de entrada com os dados da transação
     * @return DTO de resposta com os dados sincronizados e o status atualizado
     * @throws RuntimeException se a procedure retornar erro de correção relativística
     */
    @Transactional
    public TransactionResponse sincronizarTransacao(TransactionRequest request) {
        TransactionBuffer buffer = new TransactionBuffer();
        buffer.setNodePair(new CislunarNodePair(request.sourceNode(), request.targetNode()));
        buffer.setPayload(request.payload());
        buffer.setLocalTimestamp(request.localTimestamp());
        buffer.setSyncStatus("PENDING");

        TransactionBuffer saved = repository.save(buffer);

        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery("SP_CORRIGIR_TEMPO_LUNAR");
        query.registerStoredProcedureParameter("p_id_tx", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_status", String.class, ParameterMode.OUT);
        query.setParameter("p_id_tx", saved.getIdTx());
        query.execute();

        String statusProcedure = (String) query.getOutputParameterValue("p_status");
        if (statusProcedure != null && statusProcedure.startsWith("ERROR")) {
            throw new ProcedureExecutionException(statusProcedure);
        }

        TransactionBuffer syncronizado = repository.findById(saved.getIdTx())
                .orElseThrow(() -> new TransactionNotFoundException(saved.getIdTx()));

        return TransactionResponse.from(syncronizado);
    }

    /**
     * Busca uma transação pelo identificador único.
     *
     * @param id identificador da transação
     * @return DTO de resposta com os dados da transação encontrada
     * @throws TransactionNotFoundException se nenhuma transação for encontrada com o ID
     */
    public TransactionResponse buscarPorId(Long id) {
        TransactionBuffer buffer = repository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        return TransactionResponse.from(buffer);
    }

    /**
     * Lista todas as transações do buffer, com filtro opcional por status de sincronização.
     *
     * @param status filtro de status (PENDING, SYNCED, CANCELLED) — pode ser nulo para listar todas
     * @return lista de DTOs de resposta
     */
    public List<TransactionResponse> listarTransacoes(String status) {
        List<TransactionBuffer> lista = (status != null)
                ? repository.findBySyncStatus(status)
                : repository.findAll();

        return lista.stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza o status de sincronização de uma transação existente (PATCH parcial).
     *
     * <p>Suporta apenas a evolução do campo {@code syncStatus} dentro do ciclo de vida DTN:
     * {@code PENDING → SYNCED} ou {@code PENDING → CANCELLED}.</p>
     *
     * @param id      identificador da transação a ser atualizada
     * @param request DTO com o novo status validado por Bean Validation
     * @return DTO de resposta com o novo estado da transação
     * @throws TransactionNotFoundException se a transação não for encontrada
     * @throws IllegalArgumentException     se a transição de status for inválida
     */
    @Transactional
    public TransactionResponse atualizarStatus(Long id, StatusUpdateRequest request) {
        TransactionBuffer buffer = repository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        String statusAtual = buffer.getSyncStatus();
        String novoStatus = request.syncStatus();

        // Valida a transição de status: SYNCED e CANCELLED são terminais.
        if ("SYNCED".equals(statusAtual) || "CANCELLED".equals(statusAtual)) {
            throw new IllegalArgumentException(
                    "Transacao com status '" + statusAtual + "' nao pode ser alterada. Status terminal.");
        }

        buffer.setSyncStatus(novoStatus);
        TransactionBuffer atualizado = repository.save(buffer);
        return TransactionResponse.from(atualizado);
    }

    /**
     * Remove uma transação do buffer DTN pelo identificador.
     *
     * @param id identificador da transação a ser removida
     * @throws TransactionNotFoundException se a transação não existir
     */
    @Transactional
    public void deletarTransacao(Long id) {
        if (!repository.existsById(id)) {
            throw new TransactionNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
