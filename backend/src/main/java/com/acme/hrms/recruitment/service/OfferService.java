package com.acme.hrms.recruitment.service;

import java.util.List;
import java.util.UUID;

import com.acme.hrms.recruitment.dto.OfferCreateRequest;
import com.acme.hrms.recruitment.dto.OfferResponse;

public interface OfferService {
    OfferResponse createOffer(UUID applicationId, OfferCreateRequest request);
    List<OfferResponse> listOffersForApplication(UUID applicationId);
    void approveOffer(UUID id);
    void submitOfferForApproval(UUID id);
    void releaseOffer(UUID id);
    OfferResponse getOfferBySecureToken(UUID secureToken);
    OfferResponse acceptOffer(UUID secureToken);
    OfferResponse rejectOffer(UUID secureToken);
}
