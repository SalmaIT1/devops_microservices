package org.ms.produitservice.web;

import lombok.RequiredArgsConstructor;
import org.ms.produitservice.entities.Produit;
import org.ms.produitservice.repository.ProduitRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
@RestController
@RequestMapping("/produits")
@RequiredArgsConstructor
public class ProduitRestController {

    private final ProduitRepository produitRepository;
    private static final String IMAGE_DIR = "uploads/images";

    // Accessible to USER and ADMIN
    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public List<Produit> getAllProduits() {
        return produitRepository.findAll();
    }

    // Accessible to USER and ADMIN
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public Produit getProduitById(@PathVariable Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec id " + id));
    }

    // Accessible to ADMIN only
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Produit createProduit(@RequestBody Produit produit) {
        return produitRepository.save(produit);
    }

    // Accessible to ADMIN only
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public Produit updateProduit(@PathVariable Long id, @RequestBody Produit produitRequest) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec id " + id));
        produit.setName(produitRequest.getName());
        produit.setPrice(produitRequest.getPrice());
        produit.setQuantity(produitRequest.getQuantity());
        return produitRepository.save(produit);
    }
    @PostMapping("/{id}/upload-image")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Produit uploadProductImage(@PathVariable Long id, @RequestParam("image") MultipartFile imageFile) throws Exception {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec id " + id));

        // Save file locally (you can customize storage)
        Path imagePath = Paths.get(IMAGE_DIR, imageFile.getOriginalFilename());
        Files.createDirectories(imagePath.getParent());
        Files.write(imagePath, imageFile.getBytes());

        // Build image URL (assuming static serving from /uploads)
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/images/")
                .path(imageFile.getOriginalFilename())
                .toUriString();

        produit.setImageUrl(imageUrl);
        return produitRepository.save(produit);
    }
    // Accessible to ADMIN only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteProduit(@PathVariable Long id) {
        produitRepository.deleteById(id);
    }
}
