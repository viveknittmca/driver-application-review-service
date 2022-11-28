package com.taxi.partner.reviewservice.services.listeners;

import com.taxi.partner.reviewservice.config.JmsConfig;
import com.taxi.partner.reviewservice.domain.ApplicationReview;
import com.taxi.partner.reviewservice.domain.ApplicationReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ApplicationReviewStatusEnum;
import com.taxi.partner.reviewservice.repositories.ApplicationReviewRepository;
import com.taxi.partner.reviewservice.services.ApplicationReviewManager;
import com.taxi.partner.model.ApplicationDto;
import com.taxi.partner.model.events.NewApplicationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by vivek on 20/12/22.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class NewApplicationReviewListener {
    private final ApplicationReviewManager applicationReviewManager;

    private final ApplicationReviewRepository applicationReviewRepository;

    @JmsListener(destination = JmsConfig.REVIEW_REQUEST_QUEUE)
    public void listen(NewApplicationEvent newApplicationEvent){
        ApplicationDto applicationDto = newApplicationEvent.getApplicationDto();
        ApplicationReview applicationReview = ApplicationReview.builder()
                .id(null)
                .reviewStatus(ApplicationReviewStatusEnum.NEW)
                .build();

        ApplicationReview savedApplicationReview = applicationReviewRepository.saveAndFlush(applicationReview);
        applicationReviewManager.sendApplicationReviewEvent(savedApplicationReview, ApplicationReviewEventEnum.VALIDATE_APPLICATION);
    }

}
