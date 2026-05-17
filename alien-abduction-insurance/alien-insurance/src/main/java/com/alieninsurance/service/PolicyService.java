package com.alieninsurance.service;

import com.alieninsurance.dto.PolicyDto;
import com.alieninsurance.entity.Policy;
import com.alieninsurance.entity.User;
import com.alieninsurance.exception.AlienInsuranceException;
import com.alieninsurance.repository.PolicyRepository;
import com.alieninsurance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final PremiumCalculationService premiumService;

    public PolicyDto.Response createPolicy(String username, PolicyDto.CreateRequest request) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new AlienInsuranceException("User not found"));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new AlienInsuranceException("End date must be after start date");
        }

        var premium = premiumService.calculate(
            request.getResidenceZone(),
            request.getNightsOutdoorPerWeek(),
            request.getHasReportedUfos(),
            request.getHasMicrochip(),
            request.getCoverageAmount()
        );

        Policy policy = Policy.builder()
            .policyNumber(generatePolicyNumber())
            .user(user)
            .residenceZone(request.getResidenceZone().toUpperCase())
            .nightsOutdoorPerWeek(request.getNightsOutdoorPerWeek())
            .hasReportedUfos(request.getHasReportedUfos())
            .hasMicrochip(request.getHasMicrochip())
            .coverageAmount(request.getCoverageAmount())
            .monthlyPremium(premium)
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .status(Policy.PolicyStatus.ACTIVE)
            .build();

        return toResponse(policyRepository.save(policy));
    }

    public List<PolicyDto.Response> getPoliciesForUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new AlienInsuranceException("User not found"));

        return policyRepository.findByUserId(user.getId())
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public PolicyDto.Response getPolicyById(Long id, String username) {
        Policy policy = policyRepository.findById(id)
            .orElseThrow(() -> new AlienInsuranceException("Policy not found"));

        if (!policy.getUser().getUsername().equals(username)) {
            throw new AlienInsuranceException("Access denied");
        }

        return toResponse(policy);
    }

    public PolicyDto.Response cancelPolicy(Long id, String username) {
        Policy policy = policyRepository.findById(id)
            .orElseThrow(() -> new AlienInsuranceException("Policy not found"));

        if (!policy.getUser().getUsername().equals(username)) {
            throw new AlienInsuranceException("Access denied");
        }

        if (policy.getStatus() != Policy.PolicyStatus.ACTIVE) {
            throw new AlienInsuranceException("Only active policies can be cancelled");
        }

        policy.setStatus(Policy.PolicyStatus.CANCELLED);
        return toResponse(policyRepository.save(policy));
    }

    public PolicyDto.QuoteResponse getQuote(PolicyDto.QuoteRequest request) {
        return premiumService.buildQuote(request);
    }



    public Policy findActivePolicyById(Long id) {
        return policyRepository.findById(id)
            .orElseThrow(() -> new AlienInsuranceException("Policy not found"));
    }

    private PolicyDto.Response toResponse(Policy p) {
        PolicyDto.Response r = new PolicyDto.Response();
        r.setId(p.getId());
        r.setPolicyNumber(p.getPolicyNumber());
        r.setResidenceZone(p.getResidenceZone());
        r.setNightsOutdoorPerWeek(p.getNightsOutdoorPerWeek());
        r.setHasReportedUfos(p.getHasReportedUfos());
        r.setHasMicrochip(p.getHasMicrochip());
        r.setCoverageAmount(p.getCoverageAmount());
        r.setMonthlyPremium(p.getMonthlyPremium());
        r.setStartDate(p.getStartDate());
        r.setEndDate(p.getEndDate());
        r.setStatus(p.getStatus());
        r.setOwnerUsername(p.getUser().getUsername());
        return r;
    }

    private String generatePolicyNumber() {
        String candidate;
        do {
            candidate = "AAI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (policyRepository.existsByPolicyNumber(candidate));
        return candidate;
    }
}
