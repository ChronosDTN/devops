package br.com.fiap.chronos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * Objeto de valor embeddable que agrupa os identificadores dos nós
 * de origem e destino de uma transação cislunar.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CislunarNodePair implements Serializable {

    @Column(name = "source_node", nullable = false)
    private Long sourceNode;

    @Column(name = "target_node", nullable = false)
    private Long targetNode;
}