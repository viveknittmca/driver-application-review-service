package com.taxi.partner.reviewservice.services;

import com.taxi.partner.reviewservice.domain.Review;
import com.taxi.partner.reviewservice.domain.ReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ReviewStatusEnum;
import com.taxi.partner.reviewservice.repositories.ReviewRepository;
import com.taxi.partner.reviewservice.sm.ReviewStateChangeInterceptor;
import com.taxi.partner.model.ReviewDto;
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
 * Created by vivek on 01/12/22.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewManagerImpl implements ReviewManager {

    public static final String REVIEW_ID_HEADER = "REVIEW_ID_HEADER";

    private final StateMachineFactory<ReviewStatusEnum, ReviewEventEnum> stateMachineFactory;
    private final ReviewRepository reviewRepository;
    private final ReviewStateChangeInterceptor reviewStateChangeInterceptor;

    @Transactional
    @Override
    public Review newReview(Review review) {
        review.setId(null);
        review.setReviewStatus(ReviewStatusEnum.NEW);
        Review savedReview = reviewRepository.saveAndFlush(review);

        sendReviewEvent(savedReview, ReviewEventEnum.VALIDATE_REVIEW);
        return savedReview;
    }

    @Transactional
    @Override
    public void processValidationResult(UUID applicationReviewId, Boolean isValid) {
        log.debug("Process Validation Result for applicationReviewId: " + applicationReviewId + " Valid? " + isValid);

        Optional<Review> applicationReviewOptional = reviewRepository.findById(applicationReviewId);

        applicationReviewOptional.ifPresentOrElse(applicationReview -> {
            if(isValid){
                sendReviewEvent(applicationReview, ReviewEventEnum.VALIDATION_PASSED);

                //wait for status change
                awaitForStatus(applicationReviewId, ReviewStatusEnum.VALIDATED);

                Review validatedOrder = reviewRepository.findById(applicationReviewId).get();

                sendReviewEvent(validatedOrder, ReviewEventEnum.VERIFY_REVIEW);

            } else {
                sendReviewEvent(applicationReview, ReviewEventEnum.VALIDATION_FAILED);
            }
        }, () -> log.error("Review Not Found. Id: " + applicationReviewId));
    }

    @Override
    public void applicationReviewVerificationPassed(ReviewDto reviewDto) {
        Optional<Review> applicationReviewOptional = reviewRepository.findById(reviewDto.getId());

        applicationReviewOptional.ifPresentOrElse(applicationReview -> {
            sendReviewEvent(applicationReview, ReviewEventEnum.VERIFICATION_SUCCEED);
            awaitForStatus(applicationReview.getId(), ReviewStatusEnum.VERIFIED);
            updateVerifiedCount(reviewDto);
        }, () -> log.error("Review Id Not Found: " + reviewDto.getId() ));
    }

//    @Override
//    public void applicationReviewVerificationPendingDocuments(ReviewDto reviewDto) {
//        Optional<Review> applicationReviewOptional = reviewRepository.findById(reviewDto.getId());
//
//        applicationReviewOptional.ifPresentOrElse(applicationReview -> {
//            sendApplicationReviewEvent(applicationReview, ReviewEventEnum.VERIFICATION_PENDING);
//            awaitForStatus(applicationReview.getId(), ReviewStatusEnum.PENDING_DOCUMENTS);
//            updateVerifiedCount(reviewDto);
//        }, () -> log.error("Review Id Not Found: " + reviewDto.getId() ));
//
//    }

    private void updateVerifiedCount(ReviewDto reviewDto) {
        Optional<Review> verifiedApplicationOptional = reviewRepository.findById(reviewDto.getId());

//        verifiedApplicationOptional.ifPresentOrElse(verifiedApplication -> {
//            verifiedApplication.getReviewLines().forEach(applicationReviewLine -> {
//                applicationReviewDto.getApplicationReviewLines().forEach(applicationReviewLineDto -> {
//                    if(applicationReviewLine.getId() .equals(applicationReviewLineDto.getId())){
//                        applicationReviewLine.setCountVerified(applicationReviewLineDto.getCountVerified());
//                    }
//                });
//            });
//
//            reviewRepository.saveAndFlush(verifiedApplication);
//        }, () -> log.error("Review Not Found. Id: " + applicationReviewDto.getId()));
    }

    @Override
    public void applicationReviewVerificationFailed(ReviewDto reviewDto) {
        Optional<Review> applicationReviewOptional = reviewRepository.findById(reviewDto.getId());

        applicationReviewOptional.ifPresentOrElse(applicationReview -> {
            sendReviewEvent(applicationReview, ReviewEventEnum.VERIFICATION_FAILED);
        }, () -> log.error("Review Not Found. Id: " + reviewDto.getId()) );

    }

    @Override
    public void kitPickedUp(UUID id) {
        Optional<Review> applicationReviewOptional = reviewRepository.findById(id);

        applicationReviewOptional.ifPresentOrElse(applicationReview -> {
            //do process
            sendReviewEvent(applicationReview, ReviewEventEnum.SHIP_KIT);
        }, () -> log.error("Review Not Found. Id: " + id));
    }

    @Override
    public void cancelReview(UUID id) {
        reviewRepository.findById(id).ifPresentOrElse(applicationReview -> {
            sendReviewEvent(applicationReview, ReviewEventEnum.CANCEL_REVIEW);
        }, () -> log.error("Review Not Found. Id: " + id));
    }

    public void sendReviewEvent(Review review, ReviewEventEnum eventEnum){
        StateMachine<ReviewStatusEnum, ReviewEventEnum> sm = build(review);

        Message msg = MessageBuilder.withPayload(eventEnum)
                .setHeader(REVIEW_ID_HEADER, review.getId().toString())
                .build();

        sm.sendEvent(msg);
    }

    private void awaitForStatus(UUID applicationReviewId, ReviewStatusEnum statusEnum) {

        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger loopCount = new AtomicInteger(0);

        while (!found.get()) {
            if (loopCount.incrementAndGet() > 10) {
                found.set(true);
                log.debug("Loop Retries exceeded");
            }

            reviewRepository.findById(applicationReviewId).ifPresentOrElse(applicationReview -> {
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

    //Building State Machine from DB
    private StateMachine<ReviewStatusEnum, ReviewEventEnum> build(Review review){
        StateMachine<ReviewStatusEnum, ReviewEventEnum> sm = stateMachineFactory.getStateMachine(review.getId());

        sm.stop();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(reviewStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(review.getReviewStatus(), null, null, null));
                });

        sm.start();

        return sm;
    }
}
