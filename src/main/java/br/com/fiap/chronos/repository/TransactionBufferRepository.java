package br.com.fiap.chronos.repository;

import br.com.fiap.chronos.model.TransactionBuffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Interface de repositório JPA para gerenciar a persistência dos dados de transações do buffer.
 */
@Repository
public interface TransactionBufferRepository extends JpaRepository<TransactionBuffer, Long> {

    /**
     * Filtra as transações do buffer pelo status de sincronização.
     */
    List<TransactionBuffer> findBySyncStatus(String syncStatus);
}
