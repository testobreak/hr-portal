package com.acme.hrms.recruitment.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.recruitment.dto.OfferResponse;
import com.acme.hrms.recruitment.service.OfferService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/careers/offers")
@Tag(name = "Job Offers (Public)", description = "Unauthenticated endpoints for candidates to review and accept/decline offers")
public class PublicOfferController {

    private final OfferService service;

    public PublicOfferController(OfferService service) {
        this.service = service;
    }

    @GetMapping("/{secureToken}")
    @Operation(summary = "Get offer details by secure token")
    public OfferResponse getOffer(@PathVariable UUID secureToken) {
        return service.getOfferBySecureToken(secureToken);
    }

    @PostMapping("/{secureToken}/accept")
    @Operation(summary = "Accept the job offer, initiating pre-joining onboarding setup")
    public ResponseEntity<OfferResponse> accept(@PathVariable UUID secureToken) {
        OfferResponse response = service.acceptOffer(secureToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{secureToken}/reject")
    @Operation(summary = "Reject the job offer")
    public ResponseEntity<OfferResponse> reject(@PathVariable UUID secureToken) {
        OfferResponse response = service.rejectOffer(secureToken);
        return ResponseEntity.ok(response);
    }
}
