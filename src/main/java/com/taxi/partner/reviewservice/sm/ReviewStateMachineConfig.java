package com.taxi.partner.reviewservice.sm;

import com.taxi.partner.reviewservice.domain.ReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ReviewStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

/**
 * Created by vivek on 11/29/19.
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableStateMachineFactory
public class ReviewStateMachineConfig extends StateMachineConfigurerAdapter<ReviewStatusEnum, ReviewEventEnum> {

    private final Action<ReviewStatusEnum, ReviewEventEnum> validateReviewAction;
    private final Action<ReviewStatusEnum, ReviewEventEnum> verifyReviewAction;
    private final Action<ReviewStatusEnum, ReviewEventEnum>  validationFailureAction;
    private final Action<ReviewStatusEnum, ReviewEventEnum> verificationFailureAction;
    private final Action<ReviewStatusEnum, ReviewEventEnum> cancelReviewAction;

    @Override
    public void configure(StateMachineStateConfigurer<ReviewStatusEnum, ReviewEventEnum> states) throws Exception {
        states.withStates()
                .initial(ReviewStatusEnum.NEW)
                .states(EnumSet.allOf(ReviewStatusEnum.class))
                .end(ReviewStatusEnum.SHIPPED)
                .end(ReviewStatusEnum.CANCELLED)
                .end(ReviewStatusEnum.VALIDATION_EXCEPTION)
                .end(ReviewStatusEnum.VERIFICATION_EXCEPTION);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ReviewStatusEnum, ReviewEventEnum> transitions) throws Exception {
        transitions.withExternal()
                .source(ReviewStatusEnum.NEW).target(ReviewStatusEnum.VALIDATION_PENDING)
                .event(ReviewEventEnum.VALIDATE_REVIEW)
                .action(validateReviewAction)
           .and().withExternal()
                .source(ReviewStatusEnum.VALIDATION_PENDING).target(ReviewStatusEnum.VALIDATED)
                .event(ReviewEventEnum.VALIDATION_PASSED)
           .and().withExternal()
                .source(ReviewStatusEnum.VALIDATION_PENDING).target(ReviewStatusEnum.CANCELLED)
                .event(ReviewEventEnum.CANCEL_REVIEW)
           .and().withExternal()
                .source(ReviewStatusEnum.VALIDATION_PENDING).target(ReviewStatusEnum.VALIDATION_EXCEPTION)
                .event(ReviewEventEnum.VALIDATION_FAILED)
                .action(validationFailureAction)
           .and().withExternal()
                .source(ReviewStatusEnum.VALIDATED).target(ReviewStatusEnum.VERIFICATION_PENDING)
                .event(ReviewEventEnum.VERIFY_REVIEW)
                .action(verifyReviewAction)
           .and().withExternal()
                .source(ReviewStatusEnum.VALIDATED).target(ReviewStatusEnum.CANCELLED)
                .event(ReviewEventEnum.CANCEL_REVIEW)
           .and().withExternal()
                .source(ReviewStatusEnum.VERIFICATION_PENDING).target(ReviewStatusEnum.VERIFIED)
                .event(ReviewEventEnum.VERIFICATION_SUCCEED)
           .and().withExternal()
                .source(ReviewStatusEnum.VERIFICATION_PENDING).target(ReviewStatusEnum.VERIFICATION_EXCEPTION)
                .event(ReviewEventEnum.VERIFICATION_FAILED)
                .action(verificationFailureAction)
           .and().withExternal()
                .source(ReviewStatusEnum.VERIFICATION_PENDING).target(ReviewStatusEnum.CANCELLED)
                .event(ReviewEventEnum.CANCEL_REVIEW)
           .and().withExternal()
                .source(ReviewStatusEnum.VERIFIED).target(ReviewStatusEnum.SHIPPED)
                .event(ReviewEventEnum.SHIP_KIT)
           .and().withExternal()
                .source(ReviewStatusEnum.VERIFIED).target(ReviewStatusEnum.CANCELLED)
                .event(ReviewEventEnum.CANCEL_REVIEW)
                .action(cancelReviewAction);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<ReviewStatusEnum, ReviewEventEnum> config) throws Exception {
        StateMachineListenerAdapter<ReviewStatusEnum, ReviewEventEnum> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<ReviewStatusEnum, ReviewEventEnum> from, State<ReviewStatusEnum, ReviewEventEnum> to) {
                log.info(String.format("stateChanged from %s to %s", from, to));
            }
        };
        config.withConfiguration().listener(adapter);
    }
}
