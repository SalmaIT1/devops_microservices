package org.ms.factureservice.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ms.factureservice.entities.Facture;
import org.ms.factureservice.entities.FactureLigne;
import org.ms.factureservice.feign.ClientServiceClient;
import org.ms.factureservice.feign.ProduitServiceClient;
import org.ms.factureservice.model.Client;
import org.ms.factureservice.model.Produit;
import org.ms.factureservice.repository.FactureLigneRepository;
import org.ms.factureservice.repository.FactureRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/factures")
@RequiredArgsConstructor
@Slf4j
public class FactureRestController {
    
    private final FactureRepository factureRepository;
    private final FactureLigneRepository factureLigneRepository;
    private final ClientServiceClient clientServiceClient;
    private final ProduitServiceClient produitServiceClient;
    
    // ✅ Get all factures
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @GetMapping
    public List<Facture> getAllFactures() {
        List<Facture> factures = factureRepository.findAll();
        factures.forEach(facture -> {
            try {
                Client client = clientServiceClient.findClientById(facture.getClientID());
                facture.setClient(client);
            } catch (Exception e) {
                log.error("Error fetching client {} for facture {}: {}", 
                         facture.getClientID(), facture.getId(), e.getMessage());
                // Continue without setting client - or set a default client
            }
        });
        return factures;
    }
    
    // ✅ Get a single facture with details
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public Facture getFactureById(@PathVariable Long id) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture not found with id " + id));
        
        // Fetch client with error handling
        try {
            facture.setClient(clientServiceClient.findClientById(facture.getClientID()));
        } catch (Exception e) {
            log.error("Error fetching client {} for facture {}: {}", 
                     facture.getClientID(), facture.getId(), e.getMessage());
        }
        
        // Fetch products for each ligne with error handling
        facture.getFactureLignes().forEach(ligne -> {
            try {
                Produit produit = produitServiceClient.findProductById(ligne.getProduitID());
                ligne.setProduit(produit);
            } catch (Exception e) {
                log.error("Error fetching product {} for ligne {}: {}", 
                         ligne.getProduitID(), ligne.getId(), e.getMessage());
            }
        });
        
        return facture;
    }
    
    // ✅ Create a new facture
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @PostMapping
    public Facture createFacture(@RequestBody Facture facture) {
        // Vérifier que le client existe
        Client client = clientServiceClient.findClientById(facture.getClientID());
        if (client == null) throw new RuntimeException("Client introuvable");

        Facture savedFacture = factureRepository.save(facture);

        for (FactureLigne ligne : facture.getFactureLignes()) {
            Produit produit = produitServiceClient.findProductById(ligne.getProduitID());

            if (produit.getQuantity() < ligne.getQuantity()) {
                throw new RuntimeException("Stock insuffisant pour le produit : " + produit.getName());
            }

            // Décrémenter la quantité
            produit.setQuantity(produit.getQuantity() - ligne.getQuantity());

            // Mise à jour du produit dans le service produit
            produitServiceClient.updateProduit(produit.getId(), produit);

            // Enregistrer la ligne de facture
            ligne.setFacture(savedFacture);
            factureLigneRepository.save(ligne);
        }

        return savedFacture;
    }
    
    // ✅ Add a line to a facture
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @PostMapping("/{factureId}/lignes")
    public FactureLigne addLigneToFacture(@PathVariable Long factureId, @RequestBody FactureLigne ligne) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture not found"));
        ligne.setFacture(facture);
        return factureLigneRepository.save(ligne);
    }
    
    // ✅ Get all lines of a facture
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @GetMapping("/{factureId}/lignes")
    public List<FactureLigne> getFactureLignes(@PathVariable Long factureId) {
        List<FactureLigne> lignes = (List<FactureLigne>) factureLigneRepository.findByFactureId(factureId);
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
    
    // ✅ Delete facture
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @DeleteMapping("/{id}")
    public void deleteFacture(@PathVariable Long id) {
        factureRepository.deleteById(id);
    }
    
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @GetMapping("/byClient/{clientId}")
    public List<Facture> getFacturesByClientId(@PathVariable Long clientId) {
        return factureRepository.findByClientID(clientId);
    }

    // ✅ Fixed Payment method with RequestBody and BigDecimal support
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @PostMapping("/{id}/pay")
    public Facture payFacture(@PathVariable Long id, @RequestBody PaymentRequest paymentRequest) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture not found with id: " + id));

        BigDecimal paymentAmount = paymentRequest.getPaymentAmount();
        
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be positive.");
        }

        BigDecimal newPayedAmount = facture.getPayedAmount().add(paymentAmount);
        facture.setPayedAmount(newPayedAmount);

        // Automatically update status based on totalAmount
        if (newPayedAmount.compareTo(facture.getTotalAmount()) >= 0) {
            facture.setStatus(Facture.Status.PAID);
        } else {
            facture.setStatus(Facture.Status.UNPAID);
        }

        return factureRepository.save(facture);
    }
    
    // ✅ PaymentRequest DTO class with BigDecimal support
    public static class PaymentRequest {
        private BigDecimal paymentAmount;
        
        public PaymentRequest() {}
        
        public BigDecimal getPaymentAmount() {
            return paymentAmount;
        }
        
        public void setPaymentAmount(BigDecimal paymentAmount) {
            this.paymentAmount = paymentAmount;
        }
    }
}