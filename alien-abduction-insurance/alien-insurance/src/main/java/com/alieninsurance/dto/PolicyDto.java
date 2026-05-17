package com.alieninsurance.dto;

import com.alieninsurance.entity.Policy;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PolicyDto {

    @Data
    public static class CreateRequest {
        @NotBlank
        private String residenceZone;

        @NotNull @Min(0) @Max(7)
        private Integer nightsOutdoorPerWeek;

        @NotNull
        private Boolean hasReportedUfos;

        @NotNull
        private Boolean hasMicrochip;

        @NotNull @Min(10000) @Max(10000000)
        private Integer coverageAmount;

        @NotNull
        private LocalDate startDate;

        @NotNull
        private LocalDate endDate;
    }

    @Data
    public static class Response {
        private Long id;
        private String policyNumber;
        private String residenceZone;
        private Integer nightsOutdoorPerWeek;
        private Boolean hasReportedUfos;
        private Boolean hasMicrochip;
        private Integer coverageAmount;
        private BigDecimal monthlyPremium;
        private LocalDate startDate;
        private LocalDate endDate;
        private Policy.PolicyStatus status;
        private String ownerUsername;
    }

    @Data
    public static class QuoteRequest {
        @NotBlank
        private String residenceZone;

        @NotNull @Min(0) @Max(7)
        private Integer nightsOutdoorPerWeek;

        @NotNull
        private Boolean hasReportedUfos;

        @NotNull
        private Boolean hasMicrochip;

        @NotNull @Min(10000) @Max(10000000)
        private Integer coverageAmount;
    }

    @Data
    public static class QuoteResponse {
        private BigDecimal estimatedMonthlyPremium;
        private BigDecimal estimatedAnnualPremium;
        private String riskLevel;
        private String breakdown;
    }
}
