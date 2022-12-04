package com.taxi.partner.reviewservice.sm.actions;

import com.taxi.partner.reviewservice.domain.ReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ReviewStatusEnum;
import com.taxi.partner.reviewservice.services.ReviewManagerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

/**
 * Created by jt on 2/25/20.
 */
@Slf4j
@Component
public class ValidationFailureAction implements Action<ReviewStatusEnum, ReviewEventEnum> {

    @Override
    public void execute(StateContext<ReviewStatusEnum, ReviewEventEnum> context) {
        String applicationReviewId = (String) context.getMessage().getHeaders().get(ReviewManagerImpl.REVIEW_ID_HEADER);
        log.error("Compensating Transaction.... Validation Failed: " + applicationReviewId);
    }
}
