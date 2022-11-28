package com.taxi.partner.reviewservice.sm.actions;

import com.taxi.partner.reviewservice.config.JmsConfig;
import com.taxi.partner.reviewservice.domain.ApplicationReview;
import com.taxi.partner.reviewservice.domain.ApplicationReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ApplicationReviewStatusEnum;
import com.taxi.partner.reviewservice.repositories.ApplicationReviewRepository;
import com.taxi.partner.reviewservice.services.ApplicationReviewManagerImpl;
import com.taxi.partner.reviewservice.web.mappers.ApplicationReviewMapper;
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
public class VerifyReviewAction implements Action<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> {

    private final JmsTemplate jmsTemplate;
    private final ApplicationReviewRepository applicationReviewRepository;
    private final ApplicationReviewMapper applicationReviewMapper;

    @Override
    public void execute(StateContext<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> context) {
        String beerOrderId = (String) context.getMessage().getHeaders().get(ApplicationReviewManagerImpl.REVIEW_ID_HEADER);
        Optional<ApplicationReview> applicationReviewOptional = applicationReviewRepository.findById(UUID.fromString(beerOrderId));

        applicationReviewOptional.ifPresentOrElse(beerOrder -> {
                    jmsTemplate.convertAndSend(JmsConfig.VERIFY_REVIEW_QUEUE,
                            VerifyReviewRequest.builder()
                            .applicationReviewDto(applicationReviewMapper.applicationReviewToDto(beerOrder))
                            .build());
                    log.debug("Sent Allocation Request for order id: " + beerOrderId);
                }, () -> log.error("Beer Order Not Found!"));
    }
}
