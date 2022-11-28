package com.taxi.partner.reviewservice.sm;

import com.taxi.partner.reviewservice.domain.ApplicationReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ApplicationReviewStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

/**
 * Created by jt on 11/29/19.
 */
@RequiredArgsConstructor
@Configuration
@EnableStateMachineFactory
public class ApplicationReviewStateMachineConfig extends StateMachineConfigurerAdapter<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> {

    private final Action<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> validateReviewAction;
    private final Action<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> verifyReviewAction;
    private final Action<ApplicationReviewStatusEnum, ApplicationReviewEventEnum>  validationFailureAction;
    private final Action<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> verificationFailureAction;
    private final Action<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> cancelApplicationAction;

    @Override
    public void configure(StateMachineStateConfigurer<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> states) throws Exception {
        states.withStates()
                .initial(ApplicationReviewStatusEnum.NEW)
                .states(EnumSet.allOf(ApplicationReviewStatusEnum.class))
                .end(ApplicationReviewStatusEnum.PICKED_UP)
                .end(ApplicationReviewStatusEnum.SHIPPED)
                .end(ApplicationReviewStatusEnum.CANCELLED)
                .end(ApplicationReviewStatusEnum.SHIPPED_EXCEPTION)
                .end(ApplicationReviewStatusEnum.VALIDATION_EXCEPTION)
                .end(ApplicationReviewStatusEnum.VERIFICATION_EXCEPTION);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> transitions) throws Exception {
        transitions.withExternal()
                .source(ApplicationReviewStatusEnum.NEW).target(ApplicationReviewStatusEnum.VALIDATION_PENDING)
                .event(ApplicationReviewEventEnum.VALIDATE_APPLICATION)
                .action(validateReviewAction)
           .and().withExternal()
                .source(ApplicationReviewStatusEnum.VALIDATION_PENDING).target(ApplicationReviewStatusEnum.VALIDATED)
                .event(ApplicationReviewEventEnum.VALIDATION_PASSED)
           .and().withExternal()
                .source(ApplicationReviewStatusEnum.VALIDATION_PENDING).target(ApplicationReviewStatusEnum.CANCELLED)
                .event(ApplicationReviewEventEnum.CANCEL_APPLICATION)
           .and().withExternal()
                .source(ApplicationReviewStatusEnum.VALIDATION_PENDING).target(ApplicationReviewStatusEnum.VALIDATION_EXCEPTION)
                .event(ApplicationReviewEventEnum.VALIDATION_FAILED)
                .action(validationFailureAction)
            .and().withExternal()
                .source(ApplicationReviewStatusEnum.VALIDATED).target(ApplicationReviewStatusEnum.VERIFICATION_PENDING)
                .event(ApplicationReviewEventEnum.VERIFY_APPLICATION)
                .action(verifyReviewAction)
            .and().withExternal()
                .source(ApplicationReviewStatusEnum.VALIDATED).target(ApplicationReviewStatusEnum.CANCELLED)
                .event(ApplicationReviewEventEnum.CANCEL_APPLICATION)
            .and().withExternal()
                .source(ApplicationReviewStatusEnum.VERIFICATION_PENDING).target(ApplicationReviewStatusEnum.VERIFIED)
                .event(ApplicationReviewEventEnum.VERIFICATION_SUCCESS)
            .and().withExternal()
                .source(ApplicationReviewStatusEnum.VERIFICATION_PENDING).target(ApplicationReviewStatusEnum.VERIFICATION_EXCEPTION)
                .event(ApplicationReviewEventEnum.VERIFICATION_FAILED)
                .action(verificationFailureAction)
            .and().withExternal()
                .source(ApplicationReviewStatusEnum.VERIFICATION_PENDING).target(ApplicationReviewStatusEnum.CANCELLED)
                .event(ApplicationReviewEventEnum.CANCEL_APPLICATION)
                .and().withExternal()
                .source(ApplicationReviewStatusEnum.VERIFICATION_PENDING).target(ApplicationReviewStatusEnum.PENDING_DOCUMENTS)
                .event(ApplicationReviewEventEnum.VERIFICATION_PENDING)
           .and().withExternal()
                .source(ApplicationReviewStatusEnum.VERIFIED).target(ApplicationReviewStatusEnum.PICKED_UP)
                .event(ApplicationReviewEventEnum.KIT_PICKED_UP)
           .and().withExternal()
                .source(ApplicationReviewStatusEnum.VERIFIED).target(ApplicationReviewStatusEnum.CANCELLED)
                .event(ApplicationReviewEventEnum.CANCEL_APPLICATION)
                .action(cancelApplicationAction);
    }
}
