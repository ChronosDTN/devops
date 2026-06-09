package br.com.fiap.chronos.dto;

import br.com.fiap.chronos.model.TransactionBuffer;

/**
 * DTO de resposta que expõe os dados de uma transação ao cliente REST,
 * sem vazar a estrutura interna da entidade JPA {@link TransactionBuffer}.
 *
 * <p>Criado como Java Record para imutabilidade e concisão, seguindo
 * o padrão já adotado no {@link TransactionRequest}.</p>
 */
public record TransactionResponse(
        Long idTx,
        Long sourceNode,
        Long targetNode,
        String payload,
        String localTimestamp,
        String syncStatus
) {
    /**
     * Factory method que converte a entidade JPA para o DTO de resposta.
     *
     * @param buffer entidade TransactionBuffer persistida
     * @return instância do DTO pronta para serialização JSON
     */
    public static TransactionResponse from(TransactionBuffer buffer) {
        return new TransactionResponse(
                buffer.getIdTx(),
                buffer.getSourceNode(),
                buffer.getTargetNode(),
                buffer.getPayload(),
                buffer.getLocalTimestamp() != null ? buffer.getLocalTimestamp().toString() : null,
                buffer.getSyncStatus()
        );
    }
}
