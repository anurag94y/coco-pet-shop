package org.example.backend.dealer;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dealer_address_history")
public class DealerAddressHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    // getters/setters
}