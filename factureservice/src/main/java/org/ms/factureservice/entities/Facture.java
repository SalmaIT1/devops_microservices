package org.ms.factureservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.ms.factureservice.model.Client;
import java.math.BigDecimal;

import java.util.Collection;
import java.util.Date;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @ToString
public class Facture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date dateFacture;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<FactureLigne> factureLignes;

    private Long clientID;

    @Transient
    private Client client;

    // ✅ New Fields
    private BigDecimal totalAmount;

@Column(nullable = false)
private BigDecimal payedAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        PAID, UNPAID, CANCELED
    }
}
