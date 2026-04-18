package com.alieninsurance.controller;

import com.alieninsurance.dto.PolicyDto;
import com.alieninsurance.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;


    @PostMapping("/quote")
    public ResponseEntity<PolicyDto.QuoteResponse> getQuote(
            @Valid @RequestBody PolicyDto.QuoteRequest request) {
        return ResponseEntity.ok(policyService.getQuote(request));
    }


    @PostMapping
    public ResponseEntity<PolicyDto.Response> createPolicy(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody PolicyDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(policyService.createPolicy(username, request));
    }


    @GetMapping
    public ResponseEntity<List<PolicyDto.Response>> getMyPolicies(
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(policyService.getPoliciesForUser(username));
    }


    @GetMapping("/{id}")
    public ResponseEntity<PolicyDto.Response> getPolicyById(
            @AuthenticationPrincipal String username,
            @PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicyById(id, username));
    }


    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PolicyDto.Response> cancelPolicy(
            @AuthenticationPrincipal String username,
            @PathVariable Long id) {
        return ResponseEntity.ok(policyService.cancelPolicy(id, username));
    }
}
