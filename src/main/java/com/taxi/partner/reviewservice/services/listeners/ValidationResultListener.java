package com.taxi.partner.reviewservice.services.listeners;

import com.taxi.partner.reviewservice.config.JmsConfig;
import com.taxi.partner.reviewservice.services.ReviewManager;
import com.taxi.partner.model.events.ValidateReviewResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by jt on 12/2/19.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ValidationResultListener {

    private final ReviewManager reviewManager;

    @JmsListener(destination = JmsConfig.VALIDATE_REVIEW_RESPONSE_QUEUE)
    public void listen(ValidateReviewResult result){
        final UUID beerOrderId = result.getReviewId();

        log.debug("Validation Result for Order Id: " + beerOrderId);

        reviewManager.processValidationResult(beerOrderId, result.getIsValid());
    }
}
