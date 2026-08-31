package com.acme.hrms.recruitment.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.acme.hrms.recruitment.entity.Offer;

@Repository
public interface OfferRepository extends JpaRepository<Offer, UUID> {
    Optional<Offer> findBySecureToken(UUID secureToken);
    List<Offer> findByCandidateApplicationId(UUID candidateApplicationId);
}
