package br.com.fiap.chronos.exception;

/**
 * Exceção de domínio lançada quando a stored procedure SP_CORRIGIR_TEMPO_LUNAR
 * retorna um estado de erro. Mapeada para HTTP 500 com mensagem descritiva.
 */
public class ProcedureExecutionException extends RuntimeException {

    public ProcedureExecutionException(String detail) {
        super("Falha na correcao relativistica do timestamp lunar: " + detail);
    }
}