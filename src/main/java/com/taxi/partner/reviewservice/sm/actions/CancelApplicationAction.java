package com.taxi.partner.reviewservice.sm.actions;

import com.taxi.partner.reviewservice.config.JmsConfig;
import com.taxi.partner.reviewservice.domain.ApplicationReview;
import com.taxi.partner.reviewservice.domain.ApplicationReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ApplicationReviewStatusEnum;
import com.taxi.partner.reviewservice.repositories.ApplicationReviewRepository;
import com.taxi.partner.reviewservice.services.ApplicationReviewManagerImpl;
import com.taxi.partner.reviewservice.web.mappers.ApplicationReviewMapper;
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
public class CancelApplicationAction implements Action<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> {

    private final JmsTemplate jmsTemplate;
    private final ApplicationReviewRepository applicationReviewRepository;
    private final ApplicationReviewMapper applicationReviewMapper;

    @Override
    public void execute(StateContext<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> context) {
        String applicationReviewId = (String) context.getMessage().getHeaders().get(ApplicationReviewManagerImpl.REVIEW_ID_HEADER);
        Optional<ApplicationReview> beerOrderOptional = applicationReviewRepository.findById(UUID.fromString(applicationReviewId));

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            jmsTemplate.convertAndSend(JmsConfig.APPLICATION_CANCEL_QUEUE,
                    CancelApplicationRequest.builder()
                            .applicationReviewDto(applicationReviewMapper.applicationReviewToDto(beerOrder))
                            .build());
            log.debug("Sent Cancellation Request for review id: " + applicationReviewId);
        }, () -> log.error("Application Review Not Found!"));
    }
}
