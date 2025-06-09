package org.ms.reglementservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @ToString
public class Reglement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double montant;

    @Temporal(TemporalType.DATE)
    private Date datePaiement;

    private Long factureId;

    private Long clientId;
}
