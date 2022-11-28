package com.taxi.partner.reviewservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.taxi.partner.reviewservice.config.JmsConfig;
import com.taxi.partner.reviewservice.domain.ApplicationReview;
import com.taxi.partner.reviewservice.domain.ApplicationReviewLine;
import com.taxi.partner.reviewservice.domain.ApplicationReviewStatusEnum;
import com.taxi.partner.reviewservice.domain.Driver;
import com.taxi.partner.reviewservice.repositories.ApplicationReviewRepository;
import com.taxi.partner.reviewservice.repositories.DriverRepository;
import com.taxi.partner.reviewservice.services.beer.ApplicationServiceImpl;
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
public class ApplicationReviewManagerImplIT {

    @Autowired
    ApplicationReviewManager applicationReviewManager;

    @Autowired
    ApplicationReviewRepository applicationReviewRepository;

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

        ApplicationReview applicationReview = createApplicationReview();

        ApplicationReview savedApplicationReview = applicationReviewManager.newApplicationReview(applicationReview);

        await().untilAsserted(() -> {
            ApplicationReview foundOrder = applicationReviewRepository.findById(applicationReview.getId()).get();

            assertEquals(ApplicationReviewStatusEnum.VERIFIED, foundOrder.getReviewStatus());
        });

        await().untilAsserted(() -> {
            ApplicationReview foundOrder = applicationReviewRepository.findById(applicationReview.getId()).get();
            ApplicationReviewLine line = foundOrder.getApplicationReviewLines().iterator().next();
            assertEquals(line.getReviewCount(), line.getCountVerified());
        });

        ApplicationReview savedApplicationReview2 = applicationReviewRepository.findById(savedApplicationReview.getId()).get();

        assertNotNull(savedApplicationReview2);
        assertEquals(ApplicationReviewStatusEnum.VERIFIED, savedApplicationReview2.getReviewStatus());
        savedApplicationReview2.getApplicationReviewLines().forEach(line -> {
            assertEquals(line.getReviewCount(), line.getCountVerified());
        });
    }

    @Test
    void testFailedValidation() throws JsonProcessingException {
        ApplicationDto applicationDto = ApplicationDto.builder().id(applicationId).phoneNumber("12345").build();

        wireMockServer.stubFor(get(ApplicationServiceImpl.APPLICATION_PHONE_NUMBER_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(applicationDto))));

        ApplicationReview applicationReview = createApplicationReview();
        applicationReview.setDriverRef("fail-validation");

        ApplicationReview savedApplicationReview = applicationReviewManager.newApplicationReview(applicationReview);

        await().untilAsserted(() -> {
            ApplicationReview foundOrder = applicationReviewRepository.findById(applicationReview.getId()).get();

            assertEquals(ApplicationReviewStatusEnum.VALIDATION_EXCEPTION, foundOrder.getReviewStatus());
        });
    }

    @Test
    void testNewToPickedUp() throws JsonProcessingException {
        ApplicationDto applicationDto = ApplicationDto.builder().id(applicationId).phoneNumber("12345").build();

        wireMockServer.stubFor(get(ApplicationServiceImpl.APPLICATION_PHONE_NUMBER_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(applicationDto))));

        ApplicationReview applicationReview = createApplicationReview();

        ApplicationReview savedApplicationReview = applicationReviewManager.newApplicationReview(applicationReview);

        await().untilAsserted(() -> {
            ApplicationReview foundOrder = applicationReviewRepository.findById(applicationReview.getId()).get();
            assertEquals(ApplicationReviewStatusEnum.VERIFIED, foundOrder.getReviewStatus());
        });

        applicationReviewManager.kitPickedUp(savedApplicationReview.getId());

        await().untilAsserted(() -> {
            ApplicationReview foundOrder = applicationReviewRepository.findById(applicationReview.getId()).get();
            assertEquals(ApplicationReviewStatusEnum.PICKED_UP, foundOrder.getReviewStatus());
        });

        ApplicationReview pickedUpOrder = applicationReviewRepository.findById(savedApplicationReview.getId()).get();

        assertEquals(ApplicationReviewStatusEnum.PICKED_UP, pickedUpOrder.getReviewStatus());
    }

    @Test
    void testVerificationFailure() throws JsonProcessingException {
        ApplicationDto applicationDto = ApplicationDto.builder().id(applicationId).phoneNumber("12345").build();

        wireMockServer.stubFor(get(ApplicationServiceImpl.APPLICATION_PHONE_NUMBER_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(applicationDto))));

        ApplicationReview applicationReview = createApplicationReview();
        applicationReview.setDriverRef("fail-allocation");

        ApplicationReview savedApplicationReview = applicationReviewManager.newApplicationReview(applicationReview);

        await().untilAsserted(() -> {
            ApplicationReview foundOrder = applicationReviewRepository.findById(applicationReview.getId()).get();
            assertEquals(ApplicationReviewStatusEnum.VERIFICATION_EXCEPTION, foundOrder.getReviewStatus());
        });

        VerificationFailureEvent verificationFailureEvent = (VerificationFailureEvent) jmsTemplate.receiveAndConvert(JmsConfig.VERIFICATION_FAILURE_QUEUE);

        assertNotNull(verificationFailureEvent);
        assertThat(verificationFailureEvent.getReviewId()).isEqualTo(savedApplicationReview.getId());
    }

    @Test
    void testPartialVerification() throws JsonProcessingException {
        ApplicationDto applicationDto = ApplicationDto.builder().id(applicationId).phoneNumber("12345").build();

        wireMockServer.stubFor(get(ApplicationServiceImpl.APPLICATION_PHONE_NUMBER_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(applicationDto))));

        ApplicationReview applicationReview = createApplicationReview();
        applicationReview.setDriverRef("partial-allocation");

        ApplicationReview savedApplicationReview = applicationReviewManager.newApplicationReview(applicationReview);

        await().untilAsserted(() -> {
            ApplicationReview foundOrder = applicationReviewRepository.findById(applicationReview.getId()).get();
            assertEquals(ApplicationReviewStatusEnum.PENDING_DOCUMENTS, foundOrder.getReviewStatus());
        });
    }

    @Test
    void testValidationPendingToCancel() throws JsonProcessingException {
        ApplicationDto applicationDto = ApplicationDto.builder().id(applicationId).phoneNumber("12345").build();

        wireMockServer.stubFor(get(ApplicationServiceImpl.APPLICATION_PHONE_NUMBER_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(applicationDto))));

        ApplicationReview applicationReview = createApplicationReview();
        applicationReview.setDriverRef("dont-validate");

        ApplicationReview savedApplicationReview = applicationReviewManager.newApplicationReview(applicationReview);

        await().untilAsserted(() -> {
            ApplicationReview foundOrder = applicationReviewRepository.findById(applicationReview.getId()).get();
            assertEquals(ApplicationReviewStatusEnum.VALIDATION_PENDING, foundOrder.getReviewStatus());
        });

        applicationReviewManager.cancelApplication(savedApplicationReview.getId());

        await().untilAsserted(() -> {
            ApplicationReview foundOrder = applicationReviewRepository.findById(applicationReview.getId()).get();
            assertEquals(ApplicationReviewStatusEnum.CANCELLED, foundOrder.getReviewStatus());
        });
    }

    @Test
    void testVerificationPendingToCancel() throws JsonProcessingException {
        ApplicationDto applicationDto = ApplicationDto.builder().id(applicationId).phoneNumber("12345").build();

        wireMockServer.stubFor(get(ApplicationServiceImpl.APPLICATION_PHONE_NUMBER_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(applicationDto))));

        ApplicationReview applicationReview = createApplicationReview();
        applicationReview.setDriverRef("dont-allocate");

        ApplicationReview savedApplicationReview = applicationReviewManager.newApplicationReview(applicationReview);

        await().untilAsserted(() -> {
            ApplicationReview foundOrder = applicationReviewRepository.findById(applicationReview.getId()).get();
            assertEquals(ApplicationReviewStatusEnum.VERIFICATION_PENDING, foundOrder.getReviewStatus());
        });

        applicationReviewManager.cancelApplication(savedApplicationReview.getId());

        await().untilAsserted(() -> {
            ApplicationReview foundOrder = applicationReviewRepository.findById(applicationReview.getId()).get();
            assertEquals(ApplicationReviewStatusEnum.CANCELLED, foundOrder.getReviewStatus());
        });
    }

    @Test
    void testVerifiedToCancel() throws JsonProcessingException {
        ApplicationDto applicationDto = ApplicationDto.builder().id(applicationId).phoneNumber("12345").build();

        wireMockServer.stubFor(get(ApplicationServiceImpl.APPLICATION_PHONE_NUMBER_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(applicationDto))));

        ApplicationReview applicationReview = createApplicationReview();

        ApplicationReview savedApplicationReview = applicationReviewManager.newApplicationReview(applicationReview);

        await().untilAsserted(() -> {
            ApplicationReview foundOrder = applicationReviewRepository.findById(applicationReview.getId()).get();
            assertEquals(ApplicationReviewStatusEnum.VERIFIED, foundOrder.getReviewStatus());
        });

        applicationReviewManager.cancelApplication(savedApplicationReview.getId());

        await().untilAsserted(() -> {
            ApplicationReview foundOrder = applicationReviewRepository.findById(applicationReview.getId()).get();
            assertEquals(ApplicationReviewStatusEnum.CANCELLED, foundOrder.getReviewStatus());
        });

        CancelApplicationRequest cancelApplicationRequest = (CancelApplicationRequest) jmsTemplate.receiveAndConvert(JmsConfig.APPLICATION_CANCEL_QUEUE);

        assertNotNull(cancelApplicationRequest);
        assertThat(cancelApplicationRequest.getApplicationReviewDto().getId()).isEqualTo(savedApplicationReview.getId());
    }

    public ApplicationReview createApplicationReview(){
        ApplicationReview applicationReview = ApplicationReview.builder()
                .driver(testDriver)
                .build();

        Set<ApplicationReviewLine> lines = new HashSet<>();
        lines.add(ApplicationReviewLine.builder()
                .applicationId(applicationId)
                .phoneNumber("12345")
                .reviewCount(1)
                .applicationReview(applicationReview)
                .build());

        applicationReview.setApplicationReviewLines(lines);

        return applicationReview;
    }
}
