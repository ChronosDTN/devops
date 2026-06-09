package br.com.fiap.chronos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe de inicialização principal da aplicação Spring Boot.
 */
@SpringBootApplication
public class ChronosApplication {

    /**
     * Ponto de entrada padrão que inicia a aplicação Spring.
     */
    public static void main(String[] args) {
        SpringApplication.run(ChronosApplication.class, args);
    }
}
