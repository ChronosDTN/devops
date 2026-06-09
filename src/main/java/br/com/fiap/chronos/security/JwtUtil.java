package br.com.fiap.chronos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilitário responsável pela geração, validação e decodificação de tokens JWT.
 *
 * <p>O segredo e o tempo de expiração são injetados via {@code @Value} a partir
 * do {@code application.properties}, que por sua vez lê de variáveis de ambiente
 * em produção ({@code JWT_SECRET}, {@code JWT_EXPIRATION_MS}).
 * Isso evita a exposição de segredos no código-fonte e no histórico do Git.</p>
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    /**
     * Construtor que injeta o secret e o tempo de expiração do JWT via Spring {@code @Value}.
     *
     * @param secret       segredo HMAC-SHA lido do application.properties / variável de ambiente JWT_SECRET
     * @param expirationMs tempo de validade do token em milissegundos (padrão: 3600000 = 1 hora)
     */
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration.ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Gera um novo token JWT assinado para o usuário especificado.
     *
     * @param username identificador do usuário autenticado
     * @return token JWT compacto pronto para uso no header Authorization
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Valida se o token JWT pertence ao usuário informado e não está expirado.
     *
     * @param token    token JWT extraído do header Authorization
     * @param username nome de usuário esperado no subject do token
     * @return {@code true} se o token for válido e não expirado
     */
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    /**
     * Extrai o nome de usuário (Subject) contido no payload do token JWT.
     *
     * @param token token JWT a ser decodificado
     * @return subject (username) contido no payload
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Verifica se o token JWT já ultrapassou sua data de expiração.
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * Decodifica o token JWT utilizando a chave secreta e extrai todas as suas Claims.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
