package com.taxi.partner.reviewservice.services.listeners;

import com.taxi.partner.reviewservice.config.JmsConfig;
import com.taxi.partner.reviewservice.services.ReviewManager;
import com.taxi.partner.model.events.VerifyReviewResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Created by vivek on 01/12/22.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class VerificationResultListener {
    private final ReviewManager reviewManager;

    @JmsListener(destination = JmsConfig.VERIFY_REVIEW_RESPONSE_QUEUE)
    public void listen(VerifyReviewResult result){
        if(!result.getVerificationError() && !result.getPendingDocuments()){
            //allocated normally
            reviewManager.applicationReviewVerificationPassed(result.getReviewDto());
//        } else if(!result.getVerificationError() && result.getPendingDocuments()) {
//            //pending inventory
//            reviewManager.applicationReviewVerificationPendingDocuments(result.getReviewDto());
        } else if(result.getVerificationError()){
            //allocation error
            reviewManager.applicationReviewVerificationFailed(result.getReviewDto());
        }
    }

}
