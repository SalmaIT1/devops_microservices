package org.ms.factureservice.repository;
import org.ms.factureservice.entities.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import
org.springframework.data.rest.webmvc.RepositoryRestController;
import java.util.List;

@RepositoryRestController
public interface FactureRepository extends
JpaRepository<Facture, Long> {
        List<Facture> findByClientID(Long clientID);

}