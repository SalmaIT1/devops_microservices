package org.ms.factureservice.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ms.factureservice.entities.FactureLigne;
import org.ms.factureservice.feign.ProduitServiceClient;
import org.ms.factureservice.model.Produit;
import org.ms.factureservice.repository.FactureLigneRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/factureLignes")
@RequiredArgsConstructor
@Slf4j
public class FactureLigneRestController {
    
    private final FactureLigneRepository factureLigneRepository;
    private final ProduitServiceClient produitServiceClient;
    
    // ✅ Get all facture lignes
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @GetMapping
    public List<FactureLigne> getAllFactureLignes() {
        List<FactureLigne> lignes = factureLigneRepository.findAll();
        lignes.forEach(ligne -> {
            try {
                Produit produit = produitServiceClient.findProductById(ligne.getProduitID());
                ligne.setProduit(produit);
            } catch (Exception e) {
                log.error("Error fetching product {} for ligne {}: {}", 
                         ligne.getProduitID(), ligne.getId(), e.getMessage());
            }
        });
        return lignes;
    }
    
    // ✅ Get facture ligne by ID
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public FactureLigne getFactureLigneById(@PathVariable Long id) {
        FactureLigne ligne = factureLigneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FactureLigne not found with id " + id));
        
        try {
            Produit produit = produitServiceClient.findProductById(ligne.getProduitID());
            ligne.setProduit(produit);
        } catch (Exception e) {
            log.error("Error fetching product {} for ligne {}: {}", 
                     ligne.getProduitID(), ligne.getId(), e.getMessage());
        }
        
        return ligne;
    }
    
    // ✅ Create facture ligne
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @PostMapping
    public FactureLigne createFactureLigne(@RequestBody FactureLigne ligne) {
        return factureLigneRepository.save(ligne);
    }
    
    // ✅ Update facture ligne
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @PutMapping("/{id}")
    public FactureLigne updateFactureLigne(@PathVariable Long id, @RequestBody FactureLigne ligneRequest) {
        FactureLigne ligne = factureLigneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FactureLigne not found with id " + id));
        ligne.setProduitID(ligneRequest.getProduitID());
        ligne.setQuantity(ligneRequest.getQuantity());
        ligne.setPrice(ligneRequest.getPrice());
        return factureLigneRepository.save(ligne);
    }
    
    // ✅ Delete facture ligne
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @DeleteMapping("/{id}")
    public void deleteFactureLigne(@PathVariable Long id) {
        factureLigneRepository.deleteById(id);
    }
}
