package com.taxi.partner.reviewservice.services.application;

import com.taxi.partner.model.ApplicationDto;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by jt on 2019-06-09.
 */
@ConfigurationProperties(prefix = "sfg.brewery", ignoreUnknownFields = false)
@Service
public class ApplicationServiceImpl implements ApplicationService {
    public final static String APPLICATION_PATH_V1 = "/api/v1/application/";
    public final static String APPLICATION_PHONE_NUMBER_PATH_V1 = "/api/v1/applicationPhoneNumber/";
    private final RestTemplate restTemplate;

    private String applicationServiceHost;

    public ApplicationServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public Optional<ApplicationDto> getApplicationById(UUID uuid){
        return Optional.of(restTemplate.getForObject(applicationServiceHost + APPLICATION_PATH_V1 + uuid.toString(), ApplicationDto.class));
    }

    @Override
    public Optional<ApplicationDto> getApplicationByPhoneNumber(String phoneNumber) {
        return Optional.of(restTemplate.getForObject(applicationServiceHost + APPLICATION_PHONE_NUMBER_PATH_V1 + phoneNumber, ApplicationDto.class));
    }

    public void setApplicationServiceHost(String applicationServiceHost) {
        this.applicationServiceHost = applicationServiceHost;
    }
}
