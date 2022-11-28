package com.taxi.partner.reviewservice.sm.actions;

import com.taxi.partner.reviewservice.config.JmsConfig;
import com.taxi.partner.reviewservice.domain.ApplicationReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ApplicationReviewStatusEnum;
import com.taxi.partner.reviewservice.services.ApplicationReviewManagerImpl;
import com.taxi.partner.model.events.VerificationFailureEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by jt on 2/26/20.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class VerificationFailureAction implements Action<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> {

    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> context) {
        String beerOrderId = (String) context.getMessage().getHeaders().get(ApplicationReviewManagerImpl.REVIEW_ID_HEADER);

        jmsTemplate.convertAndSend(JmsConfig.VERIFICATION_FAILURE_QUEUE, VerificationFailureEvent.builder()
            .reviewId(UUID.fromString(beerOrderId))
                    .build());

        log.debug("Sent Allocation Failure Message to queue for order id " + beerOrderId);
    }
}