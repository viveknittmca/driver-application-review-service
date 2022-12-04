package com.taxi.partner.reviewservice.sm.actions;

import com.taxi.partner.reviewservice.config.JmsConfig;
import com.taxi.partner.reviewservice.domain.Review;
import com.taxi.partner.reviewservice.domain.ReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ReviewStatusEnum;
import com.taxi.partner.reviewservice.repositories.ReviewRepository;
import com.taxi.partner.reviewservice.services.ReviewManagerImpl;
import com.taxi.partner.reviewservice.web.mappers.ReviewMapper;
import com.taxi.partner.model.events.ValidateReviewRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by vivek on 01/12/22.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateReviewAction implements Action<ReviewStatusEnum, ReviewEventEnum> {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<ReviewStatusEnum, ReviewEventEnum> context) {
        String applicationReviewId = (String) context.getMessage().getHeaders().get(ReviewManagerImpl.REVIEW_ID_HEADER);
        Optional<Review> applicationReviewOptional = reviewRepository.findById(UUID.fromString(applicationReviewId));

        applicationReviewOptional.ifPresentOrElse(applicationReview -> {
            jmsTemplate.convertAndSend(JmsConfig.VALIDATE_REVIEW_QUEUE, ValidateReviewRequest.builder()
                    .applicationReview(reviewMapper.applicationReviewToDto(applicationReview))
                    .build());
        }, () -> log.error("Review Not Found. Id: " + applicationReviewId));

        log.debug("Sent Validation request to queue for review id " + applicationReviewId);
    }
}
