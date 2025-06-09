package org.ms.reglementservice.repository;

import org.ms.reglementservice.entities.Reglement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReglementRepository extends JpaRepository<Reglement, Long> {
    List<Reglement> findByClientId(Long clientId);
    List<Reglement> findByFactureId(Long factureId);
}
