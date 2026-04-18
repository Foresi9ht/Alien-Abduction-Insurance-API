package com.alieninsurance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(nullable = false)
    private LocalDateTime abductionDateTime;

    @Column(nullable = false)
    private String abductionLocation;

    @Column(nullable = false, length = 2000)
    private String incidentDescription;

    @Column(nullable = false)
    private Integer hoursMissing;

    @Column(nullable = false)
    private Boolean witnessesPresent;

    @Column(nullable = false)
    private Boolean probeEvidence;

    @Column(precision = 10, scale = 2)
    private BigDecimal claimedAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal approvedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime resolvedAt;

    private String reviewerNotes;

    public enum ClaimStatus {
        SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, PENDING_EVIDENCE
    }
}
