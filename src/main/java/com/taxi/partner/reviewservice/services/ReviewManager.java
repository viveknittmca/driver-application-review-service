package com.taxi.partner.reviewservice.services;

import com.taxi.partner.reviewservice.domain.Review;
import com.taxi.partner.reviewservice.domain.ReviewEventEnum;
import com.taxi.partner.model.ReviewDto;

import java.util.UUID;

/**
 * Created by vivek on 01/12/22.
 */
public interface ReviewManager {

    Review newReview(Review review);

    void processValidationResult(UUID applicationReviewId, Boolean isValid);

    void applicationReviewVerificationPassed(ReviewDto applicationReview);

    //void applicationReviewVerificationPendingDocuments(ReviewDto applicationReview);

    void applicationReviewVerificationFailed(ReviewDto applicationReview);

    void kitPickedUp(UUID id);

    void cancelReview(UUID id);

    void sendReviewEvent(Review review, ReviewEventEnum eventEnum);
}
