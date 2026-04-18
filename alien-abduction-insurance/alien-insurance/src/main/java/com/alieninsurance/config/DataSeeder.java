package com.alieninsurance.config;

import com.alieninsurance.entity.Claim;
import com.alieninsurance.entity.Policy;
import com.alieninsurance.entity.User;
import com.alieninsurance.repository.ClaimRepository;
import com.alieninsurance.repository.PolicyRepository;
import com.alieninsurance.repository.UserRepository;
import com.alieninsurance.service.PremiumCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final PasswordEncoder passwordEncoder;
    private final PremiumCalculationService premiumService;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded — skipping.");
            return;
        }

        log.info("Seeding database with sample alien insurance data...");


        User admin = User.builder()
            .username("admin")
            .password(passwordEncoder.encode("admin123"))
            .email("admin@alieninsurance.com")
            .role("ROLE_ADMIN")
            .build();

        User fox = User.builder()
            .username("fox_mulder")
            .password(passwordEncoder.encode("ibelieve")  )
            .email("fox@xfiles.gov")
            .role("ROLE_USER")
            .build();

        User dana = User.builder()
            .username("dana_scully")
            .password(passwordEncoder.encode("skeptic99"))
            .email("dana@xfiles.gov")
            .role("ROLE_USER")
            .build();

        userRepository.save(admin);
        userRepository.save(fox);
        userRepository.save(dana);


        BigDecimal foxPremium = premiumService.calculate("RURAL", 6, true, true, 500000);
        Policy foxPolicy = Policy.builder()
            .policyNumber("AAI-XFILES01")
            .user(fox)
            .residenceZone("RURAL")
            .nightsOutdoorPerWeek(6)
            .hasReportedUfos(true)
            .hasMicrochip(true)
            .coverageAmount(500000)
            .monthlyPremium(foxPremium)
            .startDate(LocalDate.of(2024, 1, 1))
            .endDate(LocalDate.of(2025, 12, 31))
            .status(Policy.PolicyStatus.ACTIVE)
            .build();

        BigDecimal danaPremium = premiumService.calculate("URBAN", 1, false, false, 100000);
        Policy danaPolicy = Policy.builder()
            .policyNumber("AAI-SKEPTIC1")
            .user(dana)
            .residenceZone("URBAN")
            .nightsOutdoorPerWeek(1)
            .hasReportedUfos(false)
            .hasMicrochip(false)
            .coverageAmount(100000)
            .monthlyPremium(danaPremium)
            .startDate(LocalDate.of(2024, 3, 1))
            .endDate(LocalDate.of(2025, 3, 1))
            .status(Policy.PolicyStatus.ACTIVE)
            .build();

        policyRepository.save(foxPolicy);
        policyRepository.save(danaPolicy);


        Claim foxClaim = Claim.builder()
            .claimNumber("CLM-SKYWATC1")
            .policy(foxPolicy)
            .abductionDateTime(LocalDateTime.of(2024, 11, 13, 2, 17))
            .abductionLocation("Bellefleur, Oregon")
            .incidentDescription(
                "Subject was conducting field investigation in Bellefleur forest when " +
                "a bright light descended. Lost consciousness and regained awareness " +
                "approximately 9 hours later, 3 miles from original location. Classic " +
                "triangular craft markings observed. Implant detected in nasal cavity.")
            .hoursMissing(9)
            .witnessesPresent(false)
            .probeEvidence(true)
            .claimedAmount(new BigDecimal("250000.00"))
            .status(Claim.ClaimStatus.UNDER_REVIEW)
            .submittedAt(LocalDateTime.of(2024, 11, 14, 9, 0))
            .reviewerNotes("Initial review: evidence compelling. Requesting implant analysis report.")
            .build();

        claimRepository.save(foxClaim);

        log.info("Seeding complete. Users: admin/admin123, fox_mulder/ibelieve, dana_scully/skeptic99");
    }
}
