package com.taxi.partner.reviewservice.services.testcomponets;

import com.taxi.partner.reviewservice.config.JmsConfig;
import com.taxi.partner.model.events.ValidateReviewRequest;
import com.taxi.partner.model.events.ValidateReviewResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Created by vivek on 01/12/22.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ApplicationReviewValidationListener {
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_REVIEW_QUEUE)
    public void list(Message msg){
        boolean isValid = true;
        boolean sendResponse = true;

        ValidateReviewRequest request = (ValidateReviewRequest) msg.getPayload();

        //condition to fail validation
        if (request.getApplicationReview().getDriverRef() != null) {
            if (request.getApplicationReview().getDriverRef().equals("fail-validation")){
                isValid = false;
            } else if (request.getApplicationReview().getDriverRef().equals("dont-validate")){
                sendResponse = false;
            }
        }

        if (sendResponse) {
            jmsTemplate.convertAndSend(JmsConfig.VALIDATE_REVIEW_RESPONSE_QUEUE,
                    ValidateReviewResult.builder()
                            .isValid(isValid)
                            .reviewId(request.getApplicationReview().getId())
                            .build());
        }
    }
}
