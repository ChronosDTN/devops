package br.com.fiap.chronos.exception;

import java.time.LocalDateTime;

/**
 * DTO padronizado de resposta de erro da API REST.
 * Retornado em todos os cenários de falha com estrutura consistente para o cliente.
 */
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp
) {}
