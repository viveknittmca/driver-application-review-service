package com.taxi.partner.reviewservice.sm.actions;

import com.taxi.partner.reviewservice.domain.ApplicationReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ApplicationReviewStatusEnum;
import com.taxi.partner.reviewservice.services.ApplicationReviewManagerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

/**
 * Created by jt on 2/25/20.
 */
@Slf4j
@Component
public class ValidationFailureAction implements Action<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> {

    @Override
    public void execute(StateContext<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> context) {
        String applicationReviewId = (String) context.getMessage().getHeaders().get(ApplicationReviewManagerImpl.REVIEW_ID_HEADER);
        log.error("Compensating Transaction.... Validation Failed: " + applicationReviewId);
    }
}
