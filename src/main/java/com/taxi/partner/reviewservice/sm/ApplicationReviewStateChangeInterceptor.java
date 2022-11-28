package com.taxi.partner.reviewservice.sm;

import com.taxi.partner.reviewservice.domain.ApplicationReview;
import com.taxi.partner.reviewservice.domain.ApplicationReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ApplicationReviewStatusEnum;
import com.taxi.partner.reviewservice.repositories.ApplicationReviewRepository;
import com.taxi.partner.reviewservice.services.ApplicationReviewManagerImpl;
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
 * Created by jt on 11/30/19.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationReviewStateChangeInterceptor extends StateMachineInterceptorAdapter<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> {

    private final ApplicationReviewRepository applicationReviewRepository;

    @Transactional
    @Override
    public void preStateChange(State<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> state, Message<ApplicationReviewEventEnum> message, Transition<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> transition, StateMachine<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> stateMachine) {
        log.debug("Pre-State Change");

        Optional.ofNullable(message)
                .flatMap(msg -> Optional.ofNullable((String) msg.getHeaders().getOrDefault(ApplicationReviewManagerImpl.REVIEW_ID_HEADER, " ")))
                .ifPresent(orderId -> {
                    log.debug("Saving state for order id: " + orderId + " Status: " + state.getId());

                    ApplicationReview applicationReview = applicationReviewRepository.getOne(UUID.fromString(orderId));
                    applicationReview.setReviewStatus(state.getId());
                    applicationReviewRepository.saveAndFlush(applicationReview);
                });
    }
}
