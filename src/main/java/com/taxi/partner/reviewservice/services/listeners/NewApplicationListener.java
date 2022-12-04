package com.taxi.partner.reviewservice.services.listeners;

import com.taxi.partner.model.events.UnderReviewEvent;
import com.taxi.partner.reviewservice.config.JmsConfig;
import com.taxi.partner.reviewservice.domain.Review;
import com.taxi.partner.reviewservice.services.ReviewManager;
import com.taxi.partner.model.ApplicationDto;
import com.taxi.partner.model.events.NewApplicationEvent;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * Created by vivek on 20/12/22.
 */

@Slf4j
@RequiredArgsConstructor
@Component
public class NewApplicationListener {
    private final ReviewManager reviewManager;

    private final JmsTemplate jmsTemplate;

    @Transactional
    @JmsListener(destination = JmsConfig.REVIEW_REQUEST_QUEUE)
    public void listen(NewApplicationEvent newApplicationEvent){
        ApplicationDto applicationDto = newApplicationEvent.getApplicationDto();
        log.debug("New Application Event Received For application Id: " + applicationDto.getId());
        Review review = Review.builder()
                .id(null)
                .applicationId(applicationDto.getId())
                .build();
        reviewManager.newReview(review);
        jmsTemplate.convertAndSend(JmsConfig.REVIEW_STATUS_QUEUE, new UnderReviewEvent(applicationDto));
    }

}
