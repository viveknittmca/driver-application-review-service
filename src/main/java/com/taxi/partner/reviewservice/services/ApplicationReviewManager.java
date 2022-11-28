package com.taxi.partner.reviewservice.services;

import com.taxi.partner.reviewservice.domain.ApplicationReview;
import com.taxi.partner.reviewservice.domain.ApplicationReviewEventEnum;
import com.taxi.partner.model.ApplicationReviewDto;

import java.util.UUID;

/**
 * Created by jt on 11/29/19.
 */
public interface ApplicationReviewManager {

    ApplicationReview newApplicationReview(ApplicationReview applicationReview);

    void processValidationResult(UUID applicationReviewId, Boolean isValid);

    void applicationReviewVerificationPassed(ApplicationReviewDto applicationReview);

    void applicationReviewVerificationPendingDocuments(ApplicationReviewDto applicationReview);

    void applicationReviewVerificationFailed(ApplicationReviewDto applicationReview);

    void kitPickedUp(UUID id);

    void cancelApplication(UUID id);

    void sendApplicationReviewEvent(ApplicationReview applicationReview, ApplicationReviewEventEnum eventEnum);
}
