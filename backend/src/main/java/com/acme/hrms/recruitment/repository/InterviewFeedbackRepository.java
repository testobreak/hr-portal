package com.acme.hrms.recruitment.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.acme.hrms.recruitment.entity.InterviewFeedback;

@Repository
public interface InterviewFeedbackRepository extends JpaRepository<InterviewFeedback, UUID> {
    List<InterviewFeedback> findByInterviewId(UUID interviewId);
    List<InterviewFeedback> findByInterviewCandidateApplicationId(UUID applicationId);
}
