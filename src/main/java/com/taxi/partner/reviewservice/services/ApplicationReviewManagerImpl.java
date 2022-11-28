package com.taxi.partner.reviewservice.services;

import com.taxi.partner.reviewservice.domain.ApplicationReview;
import com.taxi.partner.reviewservice.domain.ApplicationReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ApplicationReviewStatusEnum;
import com.taxi.partner.reviewservice.repositories.ApplicationReviewRepository;
import com.taxi.partner.reviewservice.sm.ApplicationReviewStateChangeInterceptor;
import com.taxi.partner.model.ApplicationReviewDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jt on 11/29/19.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ApplicationReviewManagerImpl implements ApplicationReviewManager {

    public static final String REVIEW_ID_HEADER = "REVIEW_ID_HEADER";

    private final StateMachineFactory<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> stateMachineFactory;
    private final ApplicationReviewRepository applicationReviewRepository;
    private final ApplicationReviewStateChangeInterceptor applicationReviewStateChangeInterceptor;

    @Transactional
    @Override
    public ApplicationReview newApplicationReview(ApplicationReview applicationReview) {
        applicationReview.setId(null);
        applicationReview.setReviewStatus(ApplicationReviewStatusEnum.NEW);

        ApplicationReview savedApplicationReview = applicationReviewRepository.saveAndFlush(applicationReview);
        sendApplicationReviewEvent(savedApplicationReview, ApplicationReviewEventEnum.VALIDATE_APPLICATION);
        return savedApplicationReview;
    }

    @Transactional
    @Override
    public void processValidationResult(UUID applicationReviewId, Boolean isValid) {
        log.debug("Process Validation Result for applicationReviewId: " + applicationReviewId + " Valid? " + isValid);

        Optional<ApplicationReview> applicationReviewOptional = applicationReviewRepository.findById(applicationReviewId);

        applicationReviewOptional.ifPresentOrElse(applicationReview -> {
            if(isValid){
                sendApplicationReviewEvent(applicationReview, ApplicationReviewEventEnum.VALIDATION_PASSED);

                //wait for status change
                awaitForStatus(applicationReviewId, ApplicationReviewStatusEnum.VALIDATED);

                ApplicationReview validatedOrder = applicationReviewRepository.findById(applicationReviewId).get();

                sendApplicationReviewEvent(validatedOrder, ApplicationReviewEventEnum.VERIFY_APPLICATION);

            } else {
                sendApplicationReviewEvent(applicationReview, ApplicationReviewEventEnum.VALIDATION_FAILED);
            }
        }, () -> log.error("Review Not Found. Id: " + applicationReviewId));
    }

    @Override
    public void applicationReviewVerificationPassed(ApplicationReviewDto applicationReviewDto) {
        Optional<ApplicationReview> applicationReviewOptional = applicationReviewRepository.findById(applicationReviewDto.getId());

        applicationReviewOptional.ifPresentOrElse(applicationReview -> {
            sendApplicationReviewEvent(applicationReview, ApplicationReviewEventEnum.VERIFICATION_SUCCESS);
            awaitForStatus(applicationReview.getId(), ApplicationReviewStatusEnum.VERIFIED);
            updateVerifiedCount(applicationReviewDto);
        }, () -> log.error("Review Id Not Found: " + applicationReviewDto.getId() ));
    }

    @Override
    public void applicationReviewVerificationPendingDocuments(ApplicationReviewDto applicationReviewDto) {
        Optional<ApplicationReview> applicationReviewOptional = applicationReviewRepository.findById(applicationReviewDto.getId());

        applicationReviewOptional.ifPresentOrElse(applicationReview -> {
            sendApplicationReviewEvent(applicationReview, ApplicationReviewEventEnum.VERIFICATION_PENDING);
            awaitForStatus(applicationReview.getId(), ApplicationReviewStatusEnum.PENDING_DOCUMENTS);
            updateVerifiedCount(applicationReviewDto);
        }, () -> log.error("Review Id Not Found: " + applicationReviewDto.getId() ));

    }

    private void updateVerifiedCount(ApplicationReviewDto applicationReviewDto) {
        Optional<ApplicationReview> verifiedApplicationOptional = applicationReviewRepository.findById(applicationReviewDto.getId());

        verifiedApplicationOptional.ifPresentOrElse(verifiedApplication -> {
            verifiedApplication.getApplicationReviewLines().forEach(applicationReviewLine -> {
                applicationReviewDto.getApplicationReviewLines().forEach(applicationReviewLineDto -> {
                    if(applicationReviewLine.getId() .equals(applicationReviewLineDto.getId())){
                        applicationReviewLine.setCountVerified(applicationReviewLineDto.getCountVerified());
                    }
                });
            });

            applicationReviewRepository.saveAndFlush(verifiedApplication);
        }, () -> log.error("Review Not Found. Id: " + applicationReviewDto.getId()));
    }

    @Override
    public void applicationReviewVerificationFailed(ApplicationReviewDto applicationReviewDto) {
        Optional<ApplicationReview> applicationReviewOptional = applicationReviewRepository.findById(applicationReviewDto.getId());

        applicationReviewOptional.ifPresentOrElse(applicationReview -> {
            sendApplicationReviewEvent(applicationReview, ApplicationReviewEventEnum.VERIFICATION_FAILED);
        }, () -> log.error("Review Not Found. Id: " + applicationReviewDto.getId()) );

    }

    @Override
    public void kitPickedUp(UUID id) {
        Optional<ApplicationReview> applicationReviewOptional = applicationReviewRepository.findById(id);

        applicationReviewOptional.ifPresentOrElse(applicationReview -> {
            //do process
            sendApplicationReviewEvent(applicationReview, ApplicationReviewEventEnum.KIT_PICKED_UP);
        }, () -> log.error("Review Not Found. Id: " + id));
    }

    @Override
    public void cancelApplication(UUID id) {
        applicationReviewRepository.findById(id).ifPresentOrElse(applicationReview -> {
            sendApplicationReviewEvent(applicationReview, ApplicationReviewEventEnum.CANCEL_APPLICATION);
        }, () -> log.error("Review Not Found. Id: " + id));
    }

    public void sendApplicationReviewEvent(ApplicationReview applicationReview, ApplicationReviewEventEnum eventEnum){
        StateMachine<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> sm = build(applicationReview);

        Message msg = MessageBuilder.withPayload(eventEnum)
                .setHeader(REVIEW_ID_HEADER, applicationReview.getId().toString())
                .build();

        sm.sendEvent(msg);
    }

    private void awaitForStatus(UUID applicationReviewId, ApplicationReviewStatusEnum statusEnum) {

        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger loopCount = new AtomicInteger(0);

        while (!found.get()) {
            if (loopCount.incrementAndGet() > 10) {
                found.set(true);
                log.debug("Loop Retries exceeded");
            }

            applicationReviewRepository.findById(applicationReviewId).ifPresentOrElse(applicationReview -> {
                if (applicationReview.getReviewStatus().equals(statusEnum)) {
                    found.set(true);
                    log.debug("Review Found");
                } else {
                    log.debug("Review Status Not Equal. Expected: " + statusEnum.name() + " Found: " + applicationReview.getReviewStatus().name());
                }
            }, () -> {
                log.debug("Review Id Not Found");
            });

            if (!found.get()) {
                try {
                    log.debug("Sleeping for retry");
                    Thread.sleep(100);
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
    }

    private StateMachine<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> build(ApplicationReview applicationReview){
        StateMachine<ApplicationReviewStatusEnum, ApplicationReviewEventEnum> sm = stateMachineFactory.getStateMachine(applicationReview.getId());

        sm.stop();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(applicationReviewStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(applicationReview.getReviewStatus(), null, null, null));
                });

        sm.start();

        return sm;
    }
}
