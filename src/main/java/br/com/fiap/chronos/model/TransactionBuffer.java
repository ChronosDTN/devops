package br.com.fiap.chronos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidade JPA que representa o buffer de transações financeiras cislunares.
 * Utiliza @Embedded com CislunarNodePair para modelagem avançada JPA.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_CDTN_TRANSACTION_BUFFER")
public class TransactionBuffer extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tx")
    private Long idTx;

    @Embedded
    private CislunarNodePair nodePair;

    @Column(name = "payload", length = 4000, nullable = false)
    private String payload;

    @Column(name = "local_timestamp", nullable = false)
    private LocalDateTime localTimestamp;

    @Column(name = "sync_status", length = 20, nullable = false)
    private String syncStatus = "PENDING";

    /** Atalho para o nó de origem do nodePair embeddable. */
    public Long getSourceNode() { return nodePair != null ? nodePair.getSourceNode() : null; }

    /** Atalho para o nó de destino do nodePair embeddable. */
    public Long getTargetNode() { return nodePair != null ? nodePair.getTargetNode() : null; }
}