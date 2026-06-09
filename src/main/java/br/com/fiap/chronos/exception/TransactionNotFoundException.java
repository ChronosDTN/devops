package br.com.fiap.chronos.exception;

/**
 * Exceção de domínio lançada quando uma transação não é encontrada pelo identificador.
 * Mapeada para HTTP 404 pelo GlobalExceptionHandler.
 */
public class TransactionNotFoundException extends RuntimeException {

    private final Long id;

    /**
     * Cria a exceção com mensagem padronizada contendo o ID buscado.
     *
     * @param id identificador da transação que não foi encontrada
     */
    public TransactionNotFoundException(Long id) {
        super("Transacao nao encontrada com id: " + id);
        this.id = id;
    }

    /**
     * Retorna o identificador que causou a exceção.
     */
    public Long getId() {
        return id;
    }
}
