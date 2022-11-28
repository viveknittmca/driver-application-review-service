package com.taxi.partner.reviewservice.bootstrap;

import com.taxi.partner.reviewservice.domain.Driver;
import com.taxi.partner.reviewservice.repositories.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by jt on 2019-06-06.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ApplicationReviewBootStrap implements CommandLineRunner {
    public static final String APPLICATION_SUBMIT = "Application Submit";
    public static final String APPLICATION_1_PHONE_NUM = "0631234200036";
    public static final String APPLICATION_2_PHONE_NUM = "0631234300019";
    public static final String APPLICATION_3_PHONE_NUM = "0083783375213";

    private final DriverRepository driverRepository;

    @Override
    public void run(String... args) throws Exception {
        loadDriverData();
    }

    private void loadDriverData() {
        if (driverRepository.findAllByDriverNameLike(ApplicationReviewBootStrap.APPLICATION_SUBMIT) .size() == 0) {
            Driver savedDriver = driverRepository.saveAndFlush(Driver.builder()
                    .driverName(APPLICATION_SUBMIT)
                    .apiKey(UUID.randomUUID())
                    .build());

            log.debug("Application Submitting Driver Id: " + savedDriver.getId().toString());
        }
    }
}
