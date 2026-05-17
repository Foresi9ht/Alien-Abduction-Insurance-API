package com.alieninsurance.service;

import com.alieninsurance.dto.PolicyDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PremiumCalculationService {

    private static final BigDecimal BASE_RATE = new BigDecimal("0.002");

    public BigDecimal calculate(String residenceZone,
                                int nightsOutdoor,
                                boolean hasReportedUfos,
                                boolean hasMicrochip,
                                int coverageAmount) {

        BigDecimal rate = BASE_RATE;


        rate = rate.multiply(zoneMultiplier(residenceZone));

        rate = rate.multiply(nightsMultiplier(nightsOutdoor));

        if (hasReportedUfos) {
            rate = rate.multiply(new BigDecimal("1.8"));
        }

        if (hasMicrochip) {
            rate = rate.multiply(new BigDecimal("2.5"));
        }

        BigDecimal premium = rate.multiply(new BigDecimal(coverageAmount));
        return premium.setScale(2, RoundingMode.HALF_UP);
    }

    public String determineRiskLevel(String residenceZone,
                                     int nightsOutdoor,
                                     boolean hasReportedUfos,
                                     boolean hasMicrochip) {
        int score = 0;
        score += switch (residenceZone.toUpperCase()) {
            case "AREA_51_ADJACENT" -> 4;
            case "RURAL" -> 2;
            case "SUBURBAN" -> 1;
            default -> 0;
        };
        score += nightsOutdoor / 2;
        if (hasReportedUfos) score += 3;
        if (hasMicrochip) score += 4;

        if (score >= 8) return "CRITICAL";
        if (score >= 5) return "HIGH";
        if (score >= 3) return "MEDIUM";
        return "LOW";
    }

    public String buildBreakdown(String residenceZone,
                                  int nightsOutdoor,
                                  boolean hasReportedUfos,
                                  boolean hasMicrochip) {
        StringBuilder sb = new StringBuilder();
        sb.append("Zone (").append(residenceZone).append("): ×").append(zoneMultiplier(residenceZone)).append("; ");
        sb.append("Nights outdoor (").append(nightsOutdoor).append("/week): ×").append(nightsMultiplier(nightsOutdoor)).append("; ");
        if (hasReportedUfos) sb.append("Prior UFO sightings: ×1.8; ");
        if (hasMicrochip) sb.append("Alien microchip detected: ×2.5; ");
        return sb.toString();
    }

    public PolicyDto.QuoteResponse buildQuote(PolicyDto.QuoteRequest req) {
        BigDecimal monthly = calculate(
            req.getResidenceZone(),
            req.getNightsOutdoorPerWeek(),
            req.getHasReportedUfos(),
            req.getHasMicrochip(),
            req.getCoverageAmount()
        );
        PolicyDto.QuoteResponse resp = new PolicyDto.QuoteResponse();
        resp.setEstimatedMonthlyPremium(monthly);
        resp.setEstimatedAnnualPremium(monthly.multiply(new BigDecimal("12")).setScale(2, RoundingMode.HALF_UP));
        resp.setRiskLevel(determineRiskLevel(
            req.getResidenceZone(),
            req.getNightsOutdoorPerWeek(),
            req.getHasReportedUfos(),
            req.getHasMicrochip()
        ));
        resp.setBreakdown(buildBreakdown(
            req.getResidenceZone(),
            req.getNightsOutdoorPerWeek(),
            req.getHasReportedUfos(),
            req.getHasMicrochip()
        ));
        return resp;
    }

    private BigDecimal zoneMultiplier(String zone) {
        return switch (zone.toUpperCase()) {
            case "AREA_51_ADJACENT" -> new BigDecimal("5.0");
            case "RURAL"            -> new BigDecimal("2.0");
            case "SUBURBAN"         -> new BigDecimal("1.3");
            default                 -> BigDecimal.ONE;
        };
    }

    private BigDecimal nightsMultiplier(int nights) {
        return BigDecimal.ONE
            .add(new BigDecimal("0.2").multiply(new BigDecimal(nights)))
            .setScale(2, RoundingMode.HALF_UP);
    }
}
