package com.taxi.partner.reviewservice.sm.actions;

import com.taxi.partner.reviewservice.config.JmsConfig;
import com.taxi.partner.reviewservice.domain.Review;
import com.taxi.partner.reviewservice.domain.ReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ReviewStatusEnum;
import com.taxi.partner.reviewservice.repositories.ReviewRepository;
import com.taxi.partner.reviewservice.services.ReviewManagerImpl;
import com.taxi.partner.reviewservice.web.mappers.ReviewMapper;
import com.taxi.partner.model.events.CancelApplicationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by jt on 2/29/20.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class CancelReviewAction implements Action<ReviewStatusEnum, ReviewEventEnum> {

    private final JmsTemplate jmsTemplate;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    @Override
    public void execute(StateContext<ReviewStatusEnum, ReviewEventEnum> context) {
        String applicationReviewId = (String) context.getMessage().getHeaders().get(ReviewManagerImpl.REVIEW_ID_HEADER);
        Optional<Review> beerOrderOptional = reviewRepository.findById(UUID.fromString(applicationReviewId));

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            jmsTemplate.convertAndSend(JmsConfig.APPLICATION_CANCEL_QUEUE,
                    CancelApplicationRequest.builder()
                            .reviewDto(reviewMapper.applicationReviewToDto(beerOrder))
                            .build());
            log.debug("Sent Cancellation Request for review id: " + applicationReviewId);
        }, () -> log.error("Application Review Not Found!"));
    }
}
