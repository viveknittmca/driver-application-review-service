package com.taxi.partner.reviewservice.services.testcomponets;

import com.taxi.partner.reviewservice.config.JmsConfig;
import com.taxi.partner.model.events.VerifyReviewRequest;
import com.taxi.partner.model.events.VerifyReviewResult;
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
public class ApplicationReviewVerificationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VERIFY_REVIEW_QUEUE)
    public void listen(Message msg){
        VerifyReviewRequest request = (VerifyReviewRequest) msg.getPayload();
        boolean pendingDocuments = false;
        boolean verificationError = false;
        boolean sendResponse = true;

        //set allocation error
        if (request.getReviewDto().getDriverRef() != null) {
            if (request.getReviewDto().getDriverRef().equals("fail-verification")){
                verificationError = true;
            }  else if (request.getReviewDto().getDriverRef().equals("partial-verification")) {
                pendingDocuments = true;
            } else if (request.getReviewDto().getDriverRef().equals("dont-allocate")){
                sendResponse = false;
            }
        }

        boolean finalPendingDocuments = pendingDocuments;

        request.getReviewDto().getApplicationReviewLines().forEach(applicationReviewLineDto -> {
            if (finalPendingDocuments) {
                applicationReviewLineDto.setCountVerified(applicationReviewLineDto.getReviewCount() - 1);
            } else {
                applicationReviewLineDto.setCountVerified(applicationReviewLineDto.getReviewCount());
            }
        });

        if (sendResponse) {
            jmsTemplate.convertAndSend(JmsConfig.VERIFY_REVIEW_RESPONSE_QUEUE,
                    VerifyReviewResult.builder()
                            .reviewDto(request.getReviewDto())
                            .pendingDocuments(pendingDocuments)
                            .verificationError(verificationError)
                            .build());
        }
    }
}
