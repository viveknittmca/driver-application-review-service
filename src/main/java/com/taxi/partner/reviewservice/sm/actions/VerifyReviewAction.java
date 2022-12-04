package com.taxi.partner.reviewservice.sm.actions;

import com.taxi.partner.reviewservice.config.JmsConfig;
import com.taxi.partner.reviewservice.domain.Review;
import com.taxi.partner.reviewservice.domain.ReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ReviewStatusEnum;
import com.taxi.partner.reviewservice.repositories.ReviewRepository;
import com.taxi.partner.reviewservice.services.ReviewManagerImpl;
import com.taxi.partner.reviewservice.web.mappers.ReviewMapper;
import com.taxi.partner.model.events.VerifyReviewRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by jt on 12/2/19.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VerifyReviewAction implements Action<ReviewStatusEnum, ReviewEventEnum> {

    private final JmsTemplate jmsTemplate;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    @Override
    public void execute(StateContext<ReviewStatusEnum, ReviewEventEnum> context) {
        String beerOrderId = (String) context.getMessage().getHeaders().get(ReviewManagerImpl.REVIEW_ID_HEADER);
        Optional<Review> applicationReviewOptional = reviewRepository.findById(UUID.fromString(beerOrderId));

        applicationReviewOptional.ifPresentOrElse(beerOrder -> {
                    jmsTemplate.convertAndSend(JmsConfig.VERIFY_REVIEW_QUEUE,
                            VerifyReviewRequest.builder()
                            .reviewDto(reviewMapper.applicationReviewToDto(beerOrder))
                            .build());
                    log.debug("Sent Allocation Request for order id: " + beerOrderId);
                }, () -> log.error("Beer Order Not Found!"));
    }
}
