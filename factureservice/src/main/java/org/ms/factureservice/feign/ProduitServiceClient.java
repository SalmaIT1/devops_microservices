package org.ms.factureservice.feign;
import org.ms.factureservice.config.FeignConfig;
import org.ms.factureservice.model.Produit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
@FeignClient(name = "PRODUIT-SERVICE", configuration = FeignConfig.class)
public interface ProduitServiceClient {
 @GetMapping(path="/produits")
 PagedModel<Produit> getAllProduits();
 @GetMapping(path="/produits/{id}")
 Produit findProductById(@PathVariable(name="id") Long id);
 @PutMapping("/produits/{id}")
 Produit updateProduit(@PathVariable("id") Long id, @RequestBody Produit produit);

}