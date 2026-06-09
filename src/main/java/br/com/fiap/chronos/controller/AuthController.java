package br.com.fiap.chronos.controller;

import br.com.fiap.chronos.dto.AuthRequest;
import br.com.fiap.chronos.dto.AuthResponse;
import br.com.fiap.chronos.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de autenticação responsável pela emissão de tokens JWT
 * para operadores do gateway DTN.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoint de geração de Bearer Token JWT para acesso à API")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Autentica o operador e emite um token JWT válido por 1 hora.
     * Em produção, as credenciais devem ser validadas contra um banco de usuários.
     */
    @Operation(
            summary = "Gerar token JWT",
            description = "Autentica o operador cislunar e retorna um Bearer Token. " +
                    "Use: username='operador', password='Chronos2026!' para testes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token gerado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/token")
    public ResponseEntity<AuthResponse> generateToken(@Valid @RequestBody AuthRequest request) {
        // Credencial fixa para demonstração — em produção, validar no banco de usuários
        if (!"operador".equals(request.username()) || !"Chronos2026!".equals(request.password())) {
            return ResponseEntity.status(401).build();
        }
        String token = jwtUtil.generateToken(request.username());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}