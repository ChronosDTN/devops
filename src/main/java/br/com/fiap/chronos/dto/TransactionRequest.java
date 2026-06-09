package br.com.fiap.chronos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Record que representa os dados de entrada para uma nova transação a ser sincronizada.
 */
public record TransactionRequest(
    @NotNull(message = "O no de origem e obrigatorio")
    Long sourceNode,

    @NotNull(message = "O no de destino e obrigatorio")
    Long targetNode,

    @NotBlank(message = "A payload JSON de dados e obrigatoria")
    String payload,

    @NotNull(message = "O timestamp local e obrigatorio")
    LocalDateTime localTimestamp
) {
}
