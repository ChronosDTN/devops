package br.com.fiap.chronos.dto;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "Username obrigatorio")
        String username,

        @NotBlank(message = "Password obrigatorio")
        String password
) {}
