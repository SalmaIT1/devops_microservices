package org.ms.factureservice;
 import org.ms.factureservice.entities.Facture;
 import org.ms.factureservice.entities.FactureLigne;
 import org.ms.factureservice.feign.ClientServiceClient;
 import org.ms.factureservice.feign.ProduitServiceClient;
 import org.ms.factureservice.model.Client;
 import org.ms.factureservice.model.Produit;
 import org.ms.factureservice.repository.FactureLigneRepository;
 import org.ms.factureservice.repository.FactureRepository;
 import org.springframework.boot.CommandLineRunner;
 import org.springframework.boot.SpringApplication;
 import org.springframework.boot.autoconfigure.SpringBootApplication;
 import org.springframework.cloud.openfeign.EnableFeignClients;
 import org.springframework.context.annotation.Bean;
 import org.springframework.hateoas.PagedModel;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.Date;
 import java.util.List;
 import java.util.Random;
@SpringBootApplication
@EnableMethodSecurity (prePostEnabled = true, securedEnabled = true)
@EnableFeignClients
public class FactureServiceApplication {
 public static void main(String[] args) {
 SpringApplication.run(FactureServiceApplication.class, args);
 }
 
}