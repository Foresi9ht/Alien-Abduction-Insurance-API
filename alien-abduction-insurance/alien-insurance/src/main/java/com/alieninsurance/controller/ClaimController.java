package com.alieninsurance.controller;

import com.alieninsurance.dto.ClaimDto;
import com.alieninsurance.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    public ResponseEntity<ClaimDto.Response> submitClaim(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody ClaimDto.SubmitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(claimService.submitClaim(username, request));
    }


    @GetMapping
    public ResponseEntity<List<ClaimDto.Response>> getMyClaims(
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(claimService.getClaimsForUser(username));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ClaimDto.Response> getClaimById(
            @AuthenticationPrincipal String username,
            @PathVariable Long id) {
        return ResponseEntity.ok(claimService.getClaimById(id, username));
    }


    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClaimDto.Response>> getAllClaims() {
        return ResponseEntity.ok(claimService.getAllClaims());
    }


    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimDto.Response> reviewClaim(
            @PathVariable Long id,
            @Valid @RequestBody ClaimDto.ReviewRequest request) {
        return ResponseEntity.ok(claimService.reviewClaim(id, request));
    }
}
