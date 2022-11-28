package com.taxi.partner.reviewservice.services.listeners;

import com.taxi.partner.reviewservice.config.JmsConfig;
import com.taxi.partner.reviewservice.services.ApplicationReviewManager;
import com.taxi.partner.model.events.VerifyReviewResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Created by jt on 12/3/19.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ApplicationReviewVerificationResultListener {
    private final ApplicationReviewManager applicationReviewManager;

    @JmsListener(destination = JmsConfig.VERIFY_REVIEW_RESPONSE_QUEUE)
    public void listen(VerifyReviewResult result){
        if(!result.getVerificationError() && !result.getPendingDocuments()){
            //allocated normally
            applicationReviewManager.applicationReviewVerificationPassed(result.getApplicationReviewDto());
        } else if(!result.getVerificationError() && result.getPendingDocuments()) {
            //pending inventory
            applicationReviewManager.applicationReviewVerificationPendingDocuments(result.getApplicationReviewDto());
        } else if(result.getVerificationError()){
            //allocation error
            applicationReviewManager.applicationReviewVerificationFailed(result.getApplicationReviewDto());
        }
    }

}
