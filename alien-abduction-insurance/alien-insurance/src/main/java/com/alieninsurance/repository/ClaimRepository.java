package com.alieninsurance.repository;

import com.alieninsurance.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByPolicyId(Long policyId);
    List<Claim> findByPolicyUserId(Long userId);
    Optional<Claim> findByClaimNumber(String claimNumber);
}
