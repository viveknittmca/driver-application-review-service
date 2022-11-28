package com.taxi.partner.reviewservice.services;

import com.taxi.partner.reviewservice.bootstrap.ApplicationReviewBootStrap;
import com.taxi.partner.reviewservice.domain.Driver;
import com.taxi.partner.reviewservice.repositories.ApplicationReviewRepository;
import com.taxi.partner.reviewservice.repositories.DriverRepository;
import com.taxi.partner.model.ApplicationReviewDto;
import com.taxi.partner.model.ApplicationReviewLineDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class ApplicationSubmitService {

    private final DriverRepository driverRepository;
    private final ApplicationReviewService applicationReviewService;
    private final ApplicationReviewRepository applicationReviewRepository;
    private final List<String> applicationPhoneNumbers = new ArrayList<>(3);

    public ApplicationSubmitService(DriverRepository driverRepository, ApplicationReviewService applicationReviewService,
                                    ApplicationReviewRepository applicationReviewRepository) {
        this.driverRepository = driverRepository;
        this.applicationReviewService = applicationReviewService;
        this.applicationReviewRepository = applicationReviewRepository;

        applicationPhoneNumbers.add(ApplicationReviewBootStrap.APPLICATION_1_PHONE_NUM);
        applicationPhoneNumbers.add(ApplicationReviewBootStrap.APPLICATION_2_PHONE_NUM);
        applicationPhoneNumbers.add(ApplicationReviewBootStrap.APPLICATION_3_PHONE_NUM);
    }

    @Transactional
    @Scheduled(fixedRate = 2000) //run every 2 seconds
    public void placeApplicationReview(){

        List<Driver> driverList = driverRepository.findAllByDriverNameLike(ApplicationReviewBootStrap.APPLICATION_SUBMIT);

        if (driverList.size() == 1){ //should be just one
            doPlaceReview(driverList.get(0));
        } else {
            log.error("Too many or too few applications by drivers found");

            driverList.forEach(customer -> log.debug(customer.toString()));
        }
    }

    private void doPlaceReview(Driver driver) {
        String applicationToPhoneNumber = getRandomApplicationPhoneNumber();

        ApplicationReviewLineDto applicationReviewLineDto = ApplicationReviewLineDto.builder()
                .phoneNumber(applicationToPhoneNumber)
                .reviewCount(new Random().nextInt(6)) //todo externalize value to property
                .build();

        List<ApplicationReviewLineDto> applicationReviewLineSet = new ArrayList<>();
        applicationReviewLineSet.add(applicationReviewLineDto);

        ApplicationReviewDto applicationReviewDto = ApplicationReviewDto.builder()
                .driverId(driver.getId())
                .driverRef(UUID.randomUUID().toString())
                .applicationReviewLines(applicationReviewLineSet)
                .build();

        ApplicationReviewDto savedOrder = applicationReviewService.placeReview(driver.getId(), applicationReviewDto);

    }

    private String getRandomApplicationPhoneNumber() {
        return applicationPhoneNumbers.get(new Random().nextInt(applicationPhoneNumbers.size() -0));
    }
}
