package com.taxi.partner.reviewservice.sm;

import com.taxi.partner.reviewservice.domain.Review;
import com.taxi.partner.reviewservice.domain.ReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ReviewStatusEnum;
import com.taxi.partner.reviewservice.repositories.ReviewRepository;
import com.taxi.partner.reviewservice.services.ReviewManagerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by vivek on 11/30/19.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewStateChangeInterceptor extends StateMachineInterceptorAdapter<ReviewStatusEnum, ReviewEventEnum> {

    private final ReviewRepository reviewRepository;

    @Transactional
    @Override
    public void preStateChange(State<ReviewStatusEnum, ReviewEventEnum> state, Message<ReviewEventEnum> message, Transition<ReviewStatusEnum, ReviewEventEnum> transition, StateMachine<ReviewStatusEnum, ReviewEventEnum> stateMachine) {
        log.debug("Pre-State Change");

        Optional.ofNullable(message)
                .flatMap(msg -> Optional.ofNullable((String) msg.getHeaders().getOrDefault(ReviewManagerImpl.REVIEW_ID_HEADER, "-1L")))
                .ifPresent(reviewId -> {
                    log.debug("Saving state for review id: " + reviewId + " Status: " + state.getId());

                    Review review = reviewRepository.getOne(UUID.fromString(reviewId));
                    review.setReviewStatus(state.getId());
                    reviewRepository.saveAndFlush(review);
                });
    }
}
