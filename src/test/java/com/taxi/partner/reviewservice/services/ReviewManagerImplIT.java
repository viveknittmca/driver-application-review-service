package com.taxi.partner.reviewservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.taxi.partner.reviewservice.config.JmsConfig;
import com.taxi.partner.reviewservice.domain.Review;
import com.taxi.partner.reviewservice.domain.ReviewLine;
import com.taxi.partner.reviewservice.domain.ReviewStatusEnum;
import com.taxi.partner.reviewservice.domain.Driver;
import com.taxi.partner.reviewservice.repositories.ReviewRepository;
import com.taxi.partner.reviewservice.repositories.DriverRepository;
import com.taxi.partner.reviewservice.services.application.ApplicationServiceImpl;
import com.taxi.partner.model.ApplicationDto;
import com.taxi.partner.model.events.VerificationFailureEvent;
import com.taxi.partner.model.events.CancelApplicationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.jgroups.util.Util.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by jt on 2/14/20.
 */
@ExtendWith(WireMockExtension.class)
@SpringBootTest
public class ReviewManagerImplIT {

    @Autowired
    ReviewManager reviewManager;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    DriverRepository driverRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WireMockServer wireMockServer;

    @Autowired
    JmsTemplate jmsTemplate;

    Driver testDriver;

    UUID applicationId = UUID.randomUUID();

    @TestConfiguration
    static class RestTemplateBuilderProvider {
        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer(){
            WireMockServer server = with(wireMockConfig().port(8083));
            server.start();
            return server;
        }
    }

    @BeforeEach
    void setUp() {
        testDriver = driverRepository.save(Driver.builder()
                .driverName("Test Customer")
                .build());
    }

    @Test
    void testNewToVerified() throws JsonProcessingException, InterruptedException {
        ApplicationDto applicationDto = ApplicationDto.builder().id(applicationId).phoneNumber("12345").build();

        wireMockServer.stubFor(get(ApplicationServiceImpl.APPLICATION_PHONE_NUMBER_PATH_V1 + "12345")
        .willReturn(okJson(objectMapper.writeValueAsString(applicationDto))));

        Review review = createApplicationReview();

        Review savedReview = reviewManager.newReview(review);

        await().untilAsserted(() -> {
            Review foundOrder = reviewRepository.findById(review.getId()).get();

            assertEquals(ReviewStatusEnum.VERIFIED, foundOrder.getReviewStatus());
        });

        await().untilAsserted(() -> {
            Review foundOrder = reviewRepository.findById(review.getId()).get();

        });

        Review savedReview2 = reviewRepository.findById(savedReview.getId()).get();

        assertNotNull(savedReview2);
        assertEquals(ReviewStatusEnum.VERIFIED, savedReview2.getReviewStatus());

    }

    @Test
    void testFailedValidation() throws JsonProcessingException {
        ApplicationDto applicationDto = ApplicationDto.builder().id(applicationId).phoneNumber("12345").build();

        wireMockServer.stubFor(get(ApplicationServiceImpl.APPLICATION_PHONE_NUMBER_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(applicationDto))));

        Review review = createApplicationReview();

        Review savedReview = reviewManager.newReview(review);

        await().untilAsserted(() -> {
            Review foundOrder = reviewRepository.findById(review.getId()).get();

            assertEquals(ReviewStatusEnum.VALIDATION_EXCEPTION, foundOrder.getReviewStatus());
        });
    }

    @Test
    void testNewToPickedUp() throws JsonProcessingException {
        ApplicationDto applicationDto = ApplicationDto.builder().id(applicationId).phoneNumber("12345").build();

        wireMockServer.stubFor(get(ApplicationServiceImpl.APPLICATION_PHONE_NUMBER_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(applicationDto))));

        Review review = createApplicationReview();

        Review savedReview = reviewManager.newReview(review);

        await().untilAsserted(() -> {
            Review foundOrder = reviewRepository.findById(review.getId()).get();
            assertEquals(ReviewStatusEnum.VERIFIED, foundOrder.getReviewStatus());
        });

        reviewManager.kitPickedUp(savedReview.getId());

        await().untilAsserted(() -> {
            Review foundOrder = reviewRepository.findById(review.getId()).get();
            assertEquals(ReviewStatusEnum.SHIPPED, foundOrder.getReviewStatus());
        });

        Review pickedUpOrder = reviewRepository.findById(savedReview.getId()).get();

        assertEquals(ReviewStatusEnum.SHIPPED, pickedUpOrder.getReviewStatus());
    }

    @Test
    void testVerificationFailure() throws JsonProcessingException {
        ApplicationDto applicationDto = ApplicationDto.builder().id(applicationId).phoneNumber("12345").build();

        wireMockServer.stubFor(get(ApplicationServiceImpl.APPLICATION_PHONE_NUMBER_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(applicationDto))));

        Review review = createApplicationReview();

        Review savedReview = reviewManager.newReview(review);

        await().untilAsserted(() -> {
            Review foundOrder = reviewRepository.findById(review.getId()).get();
            assertEquals(ReviewStatusEnum.VERIFICATION_EXCEPTION, foundOrder.getReviewStatus());
        });

        VerificationFailureEvent verificationFailureEvent = (VerificationFailureEvent) jmsTemplate.receiveAndConvert(JmsConfig.VERIFICATION_FAILURE_QUEUE);

        assertNotNull(verificationFailureEvent);
        assertThat(verificationFailureEvent.getReviewId()).isEqualTo(savedReview.getId());
    }

    @Test
    void testPartialVerification() throws JsonProcessingException {
        ApplicationDto applicationDto = ApplicationDto.builder().id(applicationId).phoneNumber("12345").build();

        wireMockServer.stubFor(get(ApplicationServiceImpl.APPLICATION_PHONE_NUMBER_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(applicationDto))));

        Review review = createApplicationReview();

        Review savedReview = reviewManager.newReview(review);

        await().untilAsserted(() -> {
            Review foundOrder = reviewRepository.findById(review.getId()).get();
            assertEquals(ReviewStatusEnum.VERIFICATION_PENDING, foundOrder.getReviewStatus());
        });
    }

    @Test
    void testValidationPendingToCancel() throws JsonProcessingException {
        ApplicationDto applicationDto = ApplicationDto.builder().id(applicationId).phoneNumber("12345").build();

        wireMockServer.stubFor(get(ApplicationServiceImpl.APPLICATION_PHONE_NUMBER_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(applicationDto))));

        Review review = createApplicationReview();

        Review savedReview = reviewManager.newReview(review);

        await().untilAsserted(() -> {
            Review foundOrder = reviewRepository.findById(review.getId()).get();
            assertEquals(ReviewStatusEnum.VALIDATION_PENDING, foundOrder.getReviewStatus());
        });

        reviewManager.cancelReview(savedReview.getId());

        await().untilAsserted(() -> {
            Review foundOrder = reviewRepository.findById(review.getId()).get();
            assertEquals(ReviewStatusEnum.CANCELLED, foundOrder.getReviewStatus());
        });
    }

    @Test
    void testVerificationPendingToCancel() throws JsonProcessingException {
        ApplicationDto applicationDto = ApplicationDto.builder().id(applicationId).phoneNumber("12345").build();

        wireMockServer.stubFor(get(ApplicationServiceImpl.APPLICATION_PHONE_NUMBER_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(applicationDto))));

        Review review = createApplicationReview();

        Review savedReview = reviewManager.newReview(review);

        await().untilAsserted(() -> {
            Review foundOrder = reviewRepository.findById(review.getId()).get();
            assertEquals(ReviewStatusEnum.VERIFICATION_PENDING, foundOrder.getReviewStatus());
        });

        reviewManager.cancelReview(savedReview.getId());

        await().untilAsserted(() -> {
            Review foundOrder = reviewRepository.findById(review.getId()).get();
            assertEquals(ReviewStatusEnum.CANCELLED, foundOrder.getReviewStatus());
        });
    }

    @Test
    void testVerifiedToCancel() throws JsonProcessingException {
        ApplicationDto applicationDto = ApplicationDto.builder().id(applicationId).phoneNumber("12345").build();

        wireMockServer.stubFor(get(ApplicationServiceImpl.APPLICATION_PHONE_NUMBER_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(applicationDto))));

        Review review = createApplicationReview();

        Review savedReview = reviewManager.newReview(review);

        await().untilAsserted(() -> {
            Review foundOrder = reviewRepository.findById(review.getId()).get();
            assertEquals(ReviewStatusEnum.VERIFIED, foundOrder.getReviewStatus());
        });

        reviewManager.cancelReview(savedReview.getId());

        await().untilAsserted(() -> {
            Review foundOrder = reviewRepository.findById(review.getId()).get();
            assertEquals(ReviewStatusEnum.CANCELLED, foundOrder.getReviewStatus());
        });

        CancelApplicationRequest cancelApplicationRequest = (CancelApplicationRequest) jmsTemplate.receiveAndConvert(JmsConfig.APPLICATION_CANCEL_QUEUE);

        assertNotNull(cancelApplicationRequest);
        assertThat(cancelApplicationRequest.getReviewDto().getId()).isEqualTo(savedReview.getId());
    }

    public Review createApplicationReview(){
        Review review = Review.builder()
                .build();

        Set<ReviewLine> lines = new HashSet<>();
        lines.add(ReviewLine.builder()
                .applicationId(applicationId)
                .phoneNumber("12345")
                .reviewCount(1)
                .build());
        return review;
    }
}
