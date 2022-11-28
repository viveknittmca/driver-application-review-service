package com.taxi.partner.reviewservice.sm.actions;

import com.taxi.partner.reviewservice.config.JmsConfig;
import com.taxi.partner.reviewservice.domain.ApplicationReview;
import com.taxi.partner.reviewservice.domain.ApplicationReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ApplicationReviewStatusEnum;
import com.taxi.partner.reviewservice.repositories.ApplicationReviewRepository;
import com.taxi.partner.reviewservice.services.ApplicationReviewManagerImpl;
import com.taxi.partner.reviewservice.web.mappers.ApplicationReviewMapper;
import com.taxi.partner.model.events.ValidateReviewRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by jt on 11/30/19.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateReviewAction implements Action<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> {

    private final ApplicationReviewRepository applicationReviewRepository;
    private final ApplicationReviewMapper applicationReviewMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> context) {
        String applicationReviewId = (String) context.getMessage().getHeaders().get(ApplicationReviewManagerImpl.REVIEW_ID_HEADER);
        Optional<ApplicationReview> applicationReviewOptional = applicationReviewRepository.findById(UUID.fromString(applicationReviewId));

        applicationReviewOptional.ifPresentOrElse(applicationReview -> {
            jmsTemplate.convertAndSend(JmsConfig.VALIDATE_REVIEW_QUEUE, ValidateReviewRequest.builder()
                    .applicationReview(applicationReviewMapper.applicationReviewToDto(applicationReview))
                    .build());
        }, () -> log.error("Review Not Found. Id: " + applicationReviewId));

        log.debug("Sent Validation request to queue for review id " + applicationReviewId);
    }
}
