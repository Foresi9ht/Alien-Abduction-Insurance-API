package com.alieninsurance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String policyNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(nullable = false)
    private String residenceZone;

    @Column(nullable = false)
    private Integer nightsOutdoorPerWeek;

    @Column(nullable = false)
    private Boolean hasReportedUfos;

    @Column(nullable = false)
    private Boolean hasMicrochip;

    @Column(nullable = false)
    private Integer coverageAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPremium;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Claim> claims;

    public enum PolicyStatus {
        ACTIVE, EXPIRED, CANCELLED, PENDING
    }
}
