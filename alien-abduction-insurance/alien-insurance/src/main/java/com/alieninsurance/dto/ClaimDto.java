package com.alieninsurance.dto;

import com.alieninsurance.entity.Claim;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ClaimDto {

    @Data
    public static class SubmitRequest {
        @NotNull
        private Long policyId;

        @NotNull
        private LocalDateTime abductionDateTime;

        @NotBlank
        private String abductionLocation;

        @NotBlank @Size(min = 50, max = 2000)
        private String incidentDescription;

        @NotNull @Min(1)
        private Integer hoursMissing;

        @NotNull
        private Boolean witnessesPresent;

        @NotNull
        private Boolean probeEvidence;

        @NotNull @DecimalMin("0.0")
        private BigDecimal claimedAmount;
    }

    @Data
    public static class Response {
        private Long id;
        private String claimNumber;
        private String policyNumber;
        private LocalDateTime abductionDateTime;
        private String abductionLocation;
        private String incidentDescription;
        private Integer hoursMissing;
        private Boolean witnessesPresent;
        private Boolean probeEvidence;
        private BigDecimal claimedAmount;
        private BigDecimal approvedAmount;
        private Claim.ClaimStatus status;
        private LocalDateTime submittedAt;
        private String reviewerNotes;
    }

    @Data
    public static class ReviewRequest {
        @NotNull
        private Claim.ClaimStatus status;
        private BigDecimal approvedAmount;
        private String reviewerNotes;
    }
}
