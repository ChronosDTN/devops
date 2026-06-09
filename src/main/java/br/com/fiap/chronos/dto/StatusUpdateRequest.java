package br.com.fiap.chronos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de entrada para a atualização parcial do status de uma transação DTN.
 * Suporta apenas a transição do campo {@code syncStatus}, evitando
 * atualização de campos imutáveis como payload e timestamps.
 */
public record StatusUpdateRequest(

        /**
         * Novo status da transação. Aceita apenas os valores do ciclo de vida DTN:
         * PENDING, SYNCED ou CANCELLED.
         */
        @NotBlank(message = "O novo status nao pode ser vazio")
        @Pattern(
                regexp = "PENDING|SYNCED|CANCELLED",
                message = "Status invalido. Valores aceitos: PENDING, SYNCED, CANCELLED"
        )
        String syncStatus
) {}
