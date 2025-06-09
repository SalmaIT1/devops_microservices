package org.ms.reglementservice.web;

import lombok.RequiredArgsConstructor;
import org.ms.reglementservice.entities.Reglement;
import org.ms.reglementservice.repository.ReglementRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reglements")
@RequiredArgsConstructor
public class ReglementRestController {

    private final ReglementRepository reglementRepository;

    // Récupérer tous les règlements - accessible à tous les utilisateurs authentifiés
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public List<Reglement> getAllReglements() {
        return reglementRepository.findAll();
    }

    // Récupérer un règlement par ID - accessible à tous les utilisateurs authentifiés
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public Reglement getReglementById(@PathVariable Long id) {
        return reglementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Règlement non trouvé avec id " + id));
    }

    // Récupérer les règlements par clientId - accessible à tous les utilisateurs authentifiés
    @GetMapping("/byClient/{clientId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public List<Reglement> getReglementsByClientId(@PathVariable Long clientId) {
        return reglementRepository.findByClientId(clientId);
    }

    // Récupérer les règlements par factureId - accessible à tous les utilisateurs authentifiés
    @GetMapping("/byFacture/{factureId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public List<Reglement> getReglementsByFactureId(@PathVariable Long factureId) {
        return reglementRepository.findByFactureId(factureId);
    }

    // Créer un nouveau règlement - accessible seulement aux ADMIN et USER
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public Reglement createReglement(@RequestBody Reglement reglement) {
        return reglementRepository.save(reglement);
    }

    // Mettre à jour un règlement existant - accessible seulement aux ADMIN et USER
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public Reglement updateReglement(@PathVariable Long id, @RequestBody Reglement reglementRequest) {
        Reglement reglement = reglementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Règlement non trouvé avec id " + id));
        
        reglement.setMontant(reglementRequest.getMontant());
        reglement.setDatePaiement(reglementRequest.getDatePaiement());
        reglement.setClientId(reglementRequest.getClientId());
        reglement.setFactureId(reglementRequest.getFactureId());
        
        return reglementRepository.save(reglement);
    }

    // Supprimer un règlement - accessible seulement aux ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteReglement(@PathVariable Long id) {
        reglementRepository.deleteById(id);
    }
}