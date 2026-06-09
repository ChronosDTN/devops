package br.com.fiap.chronos.model;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * Classe base de auditoria herdada por todas as entidades do domínio.
 * Utiliza @MappedSuperclass para implementar herança JPA sem tabela própria.
 */
@Getter
@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Preenche automaticamente as datas de auditoria ao persistir. */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /** Atualiza a data de modificação a cada update. */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}