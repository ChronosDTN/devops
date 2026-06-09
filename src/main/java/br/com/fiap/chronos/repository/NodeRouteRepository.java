package br.com.fiap.chronos.repository;

import br.com.fiap.chronos.model.NodeRoute;
import br.com.fiap.chronos.model.NodeRouteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositório JPA para gerenciar rotas entre nós DTN.
 */
@Repository
public interface NodeRouteRepository extends JpaRepository<NodeRoute, NodeRouteId> {
    List<NodeRoute> findByActiveTrue();
}