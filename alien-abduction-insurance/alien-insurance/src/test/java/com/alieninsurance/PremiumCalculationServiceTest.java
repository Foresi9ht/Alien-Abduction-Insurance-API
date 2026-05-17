package com.alieninsurance;

import com.alieninsurance.service.PremiumCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PremiumCalculationServiceTest {

    private PremiumCalculationService service;

    @BeforeEach
    void setUp() {
        service = new PremiumCalculationService();
    }

    @Test
    void urbanUserLowRisk_shouldReturnBasePremium() {
        BigDecimal premium = service.calculate("URBAN", 0, false, false, 100000);
        // base rate 0.002 × 1.0 (urban) × 1.0 (0 nights) × 100000 = 200.00
        assertEquals(new BigDecimal("200.00"), premium);
    }

    @Test
    void area51User_shouldHaveHighestMultiplier() {
        BigDecimal area51Premium = service.calculate("AREA_51_ADJACENT", 0, false, false, 100000);
        BigDecimal urbanPremium  = service.calculate("URBAN",            0, false, false, 100000);
        assertTrue(area51Premium.compareTo(urbanPremium) > 0,
            "Area 51 adjacent should cost more than urban");
    }

    @Test
    void microchipAndUfos_shouldStackMultipliers() {
        BigDecimal base     = service.calculate("URBAN", 0, false, false, 100000);
        BigDecimal enhanced = service.calculate("URBAN", 0, true,  true,  100000);
        // should be base × 1.8 × 2.5 = base × 4.5
        BigDecimal expected = base.multiply(new BigDecimal("4.5"))
            .setScale(2, java.math.RoundingMode.HALF_UP);
        assertEquals(expected, enhanced);
    }

    @Test
    void riskLevel_area51WithMicrochipAndUfos_shouldBeCritical() {
        String level = service.determineRiskLevel("AREA_51_ADJACENT", 7, true, true);
        assertEquals("CRITICAL", level);
    }

    @Test
    void riskLevel_urbanNewUser_shouldBeLow() {
        String level = service.determineRiskLevel("URBAN", 0, false, false);
        assertEquals("LOW", level);
    }
}
