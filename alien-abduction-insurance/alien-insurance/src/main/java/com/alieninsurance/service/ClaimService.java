package com.alieninsurance.service;

import com.alieninsurance.config.NasaApodClient;
import com.alieninsurance.dto.ClaimDto;
import com.alieninsurance.entity.Claim;
import com.alieninsurance.entity.Policy;
import com.alieninsurance.exception.AlienInsuranceException;
import com.alieninsurance.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyService policyService;
    private final NasaApodClient nasaApodClient;

    @Value("${nasa.api.key}")
    private String nasaApiKey;

    public ClaimDto.Response submitClaim(String username, ClaimDto.SubmitRequest request) {
        Policy policy = policyService.findActivePolicyById(request.getPolicyId());

        if (!policy.getUser().getUsername().equals(username)) {
            throw new AlienInsuranceException("This policy does not belong to you");
        }

        if (policy.getStatus() != Policy.PolicyStatus.ACTIVE) {
            throw new AlienInsuranceException("Claims can only be submitted on active policies");
        }

        if (request.getClaimedAmount().compareTo(new BigDecimal(policy.getCoverageAmount())) > 0) {
            throw new AlienInsuranceException(
                "Claimed amount exceeds coverage limit of $" + policy.getCoverageAmount());
        }


        String abductionDate = request.getAbductionDateTime().toLocalDate().toString();
        Map<String, Object> cosmicContext = fetchCosmicContext(abductionDate);

        Claim claim = Claim.builder()
            .claimNumber(generateClaimNumber())
            .policy(policy)
            .abductionDateTime(request.getAbductionDateTime())
            .abductionLocation(request.getAbductionLocation())
            .incidentDescription(request.getIncidentDescription())
            .hoursMissing(request.getHoursMissing())
            .witnessesPresent(request.getWitnessesPresent())
            .probeEvidence(request.getProbeEvidence())
            .claimedAmount(request.getClaimedAmount())
            .status(Claim.ClaimStatus.SUBMITTED)
            .submittedAt(LocalDateTime.now())
            .reviewerNotes("Cosmic context on abduction date: " + cosmicContext.get("title"))
            .build();

        return toResponse(claimRepository.save(claim));
    }

    public List<ClaimDto.Response> getClaimsForUser(String username) {
        return claimRepository.findByPolicyUserId(getUserIdByUsername(username))
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public ClaimDto.Response getClaimById(Long id, String username) {
        Claim claim = claimRepository.findById(id)
            .orElseThrow(() -> new AlienInsuranceException("Claim not found"));

        if (!claim.getPolicy().getUser().getUsername().equals(username)) {
            throw new AlienInsuranceException("Access denied");
        }

        return toResponse(claim);
    }

    public List<ClaimDto.Response> getAllClaims() {
        return claimRepository.findAll()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public ClaimDto.Response reviewClaim(Long id, ClaimDto.ReviewRequest request) {
        Claim claim = claimRepository.findById(id)
            .orElseThrow(() -> new AlienInsuranceException("Claim not found"));

        if (claim.getStatus() == Claim.ClaimStatus.APPROVED
                || claim.getStatus() == Claim.ClaimStatus.REJECTED) {
            throw new AlienInsuranceException("Claim has already been resolved");
        }

        claim.setStatus(request.getStatus());
        claim.setApprovedAmount(request.getApprovedAmount());
        claim.setReviewerNotes(request.getReviewerNotes());
        claim.setResolvedAt(LocalDateTime.now());

        return toResponse(claimRepository.save(claim));
    }



    private Map<String, Object> fetchCosmicContext(String date) {
        try {
            return nasaApodClient.getAstronomyPictureOfDay(nasaApiKey, date);
        } catch (Exception e) {
            return Map.of("title", "Cosmic data unavailable");
        }
    }

    private ClaimDto.Response toResponse(Claim c) {
        ClaimDto.Response r = new ClaimDto.Response();
        r.setId(c.getId());
        r.setClaimNumber(c.getClaimNumber());
        r.setPolicyNumber(c.getPolicy().getPolicyNumber());
        r.setAbductionDateTime(c.getAbductionDateTime());
        r.setAbductionLocation(c.getAbductionLocation());
        r.setIncidentDescription(c.getIncidentDescription());
        r.setHoursMissing(c.getHoursMissing());
        r.setWitnessesPresent(c.getWitnessesPresent());
        r.setProbeEvidence(c.getProbeEvidence());
        r.setClaimedAmount(c.getClaimedAmount());
        r.setApprovedAmount(c.getApprovedAmount());
        r.setStatus(c.getStatus());
        r.setSubmittedAt(c.getSubmittedAt());
        r.setReviewerNotes(c.getReviewerNotes());
        return r;
    }

    private Long getUserIdByUsername(String username) {
        return claimRepository.findByPolicyUserId(
            claimRepository.findAll().stream()
                .filter(c -> c.getPolicy().getUser().getUsername().equals(username))
                .map(c -> c.getPolicy().getUser().getId())
                .findFirst()
                .orElse(-1L)
        ).stream()
            .findFirst()
            .map(c -> c.getPolicy().getUser().getId())
            .orElse(-1L);
    }

    private String generateClaimNumber() {
        return "CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
