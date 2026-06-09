package br.com.fiap.chronos.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Tratador global de exceções da API REST via {@code @RestControllerAdvice}.
 *
 * <p>Intercepta exceções lançadas em qualquer camada da aplicação e as converte
 * em respostas HTTP padronizadas com o status correto, eliminando o retorno
 * genérico de HTTP 500 para erros de negócio conhecidos.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata o caso de recurso não encontrado, retornando HTTP 404.
     * Evita que o cliente receba HTTP 500 quando uma transação não existe.
     *
     * @param ex      exceção lançada pela camada de serviço
     * @param request requisição HTTP que originou o erro
     * @return resposta padronizada com status 404 e mensagem descritiva
     */
    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            TransactionNotFoundException ex, HttpServletRequest request) {

        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Trata erros de validação do Bean Validation (@Valid), retornando HTTP 422.
     * Agrega todas as mensagens de campo inválido em uma única string.
     *
     * @param ex      exceção de validação com a lista de erros de campo
     * @param request requisição HTTP que originou o erro
     * @return resposta padronizada com status 422 e detalhes dos campos inválidos
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String mensagens = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Unprocessable Entity",
                mensagens,
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    /**
     * Trata erros de estado inválido no processamento das transações, retornando HTTP 400.
     *
     * @param ex      exceção de argumento ilegal (ex: transição de status inválida)
     * @param request requisição HTTP que originou o erro
     * @return resposta padronizada com status 400 e mensagem do erro
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ProcedureExecutionException.class)
    public ResponseEntity<ApiErrorResponse> handleProcedure(
            ProcedureExecutionException ex, HttpServletRequest request) {

        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Procedure Execution Error",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    /**
     * Fallback genérico para erros inesperados, retornando HTTP 500.
     * Garante que o cliente nunca receba a stack trace Java bruta.
     *
     * @param ex      qualquer exceção não tratada pelos handlers anteriores
     * @param request requisição HTTP que originou o erro
     * @return resposta padronizada com status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Erro interno do gateway DTN. Contate o operador cislunar.",
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
