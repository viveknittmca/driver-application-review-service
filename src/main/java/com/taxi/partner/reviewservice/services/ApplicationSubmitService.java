package com.taxi.partner.reviewservice.services;

import com.taxi.partner.reviewservice.bootstrap.ReviewBootStrap;
import com.taxi.partner.reviewservice.domain.Driver;
import com.taxi.partner.reviewservice.repositories.ReviewRepository;
import com.taxi.partner.reviewservice.repositories.DriverRepository;
import com.taxi.partner.model.ReviewDto;
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
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final List<String> applicationPhoneNumbers = new ArrayList<>(3);

    public ApplicationSubmitService(DriverRepository driverRepository, ReviewService reviewService,
                                    ReviewRepository reviewRepository) {
        this.driverRepository = driverRepository;
        this.reviewService = reviewService;
        this.reviewRepository = reviewRepository;

        applicationPhoneNumbers.add(ReviewBootStrap.APPLICATION_1_PHONE_NUM);
        applicationPhoneNumbers.add(ReviewBootStrap.APPLICATION_2_PHONE_NUM);
        applicationPhoneNumbers.add(ReviewBootStrap.APPLICATION_3_PHONE_NUM);
    }

    @Transactional
    @Scheduled(fixedRate = 2000) //run every 2 seconds
    public void placeApplicationReview(){

        List<Driver> driverList = driverRepository.findAllByDriverNameLike(ReviewBootStrap.APPLICATION_SUBMIT);

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

        ReviewDto reviewDto = ReviewDto.builder()
                .driverId(driver.getId())
                .driverRef(UUID.randomUUID().toString())
                .applicationReviewLines(applicationReviewLineSet)
                .build();

        ReviewDto savedOrder = reviewService.placeReview(reviewDto);

    }

    private String getRandomApplicationPhoneNumber() {
        return applicationPhoneNumbers.get(new Random().nextInt(applicationPhoneNumbers.size() -0));
    }
}
