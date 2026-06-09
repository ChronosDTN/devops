package br.com.fiap.chronos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade JPA que representa a tabela de rotas entre nós da rede DTN.
 * Utiliza chave composta @EmbeddedId (NodeRouteId) e herda de BaseEntity.
 * Segunda tabela do domínio, comprovando modelagem com múltiplas entidades.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_CDTN_NODE_ROUTE")
public class NodeRoute extends BaseEntity {

    @EmbeddedId
    private NodeRouteId id;

    @Column(name = "latency_ms")
    private Double latencyMs;

    @Column(name = "hops")
    private Integer hops;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}