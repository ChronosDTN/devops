package br.com.fiap.chronos.dto;

/**
 * DTO de resposta com o token JWT gerado para o operador autenticado.
 */
public record AuthResponse(String token, String type) {
    public AuthResponse(String token) {
        this(token, "Bearer");
    }
}