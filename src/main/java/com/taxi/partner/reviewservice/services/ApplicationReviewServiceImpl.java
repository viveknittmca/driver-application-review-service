/*
 *  Copyright 2019 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.taxi.partner.reviewservice.services;

import com.taxi.partner.reviewservice.domain.ApplicationReview;
import com.taxi.partner.reviewservice.domain.ApplicationReviewStatusEnum;
import com.taxi.partner.reviewservice.domain.Driver;
import com.taxi.partner.reviewservice.repositories.ApplicationReviewRepository;
import com.taxi.partner.reviewservice.repositories.DriverRepository;
import com.taxi.partner.reviewservice.web.mappers.ApplicationReviewMapper;
import com.taxi.partner.model.ApplicationReviewDto;
import com.taxi.partner.model.ApplicationReviewPagedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationReviewServiceImpl implements ApplicationReviewService {

    private final DriverRepository driverRepository;
    private final ApplicationReviewRepository applicationReviewRepository;
    private final ApplicationReviewManager applicationReviewManager;
    private final ApplicationReviewMapper applicationReviewMapper;

    @Override
    public ApplicationReviewPagedList listReviews(UUID customerId, Pageable pageable) {
        Optional<Driver> customerOptional = driverRepository.findById(customerId);

        if (customerOptional.isPresent()) {
            Page<ApplicationReview> applicationReviewPage =
                    applicationReviewRepository.findAllByDriver(customerOptional.get(), pageable);

            return new ApplicationReviewPagedList(applicationReviewPage
                    .stream()
                    .map(applicationReviewMapper::applicationReviewToDto)
                    .collect(Collectors.toList()), PageRequest.of(
                    applicationReviewPage.getPageable().getPageNumber(),
                    applicationReviewPage.getPageable().getPageSize()),
                    applicationReviewPage.getTotalElements());
        } else {
            return null;
        }
    }

    @Transactional
    @Override
    public ApplicationReviewDto placeReview(UUID driverId, ApplicationReviewDto applicationReviewDto) {
        Optional<Driver> driverOptional = driverRepository.findById(driverId);

        if (driverOptional.isPresent()) {
            ApplicationReview applicationReview = applicationReviewMapper.dtoToApplicationReview(applicationReviewDto);
            applicationReview.setId(null); //should not be set by outside client
            applicationReview.setReviewStatus(ApplicationReviewStatusEnum.NEW);
            applicationReview.setDriver(driverOptional.get());
            
            applicationReview.getApplicationReviewLines().forEach(line -> line.setApplicationReview(applicationReview));

            ApplicationReview savedApplicationReview = applicationReviewManager.newApplicationReview(applicationReview);

            log.debug("Saved Application Review: " + applicationReview.getId());

            return applicationReviewMapper.applicationReviewToDto(savedApplicationReview);
        }
        //todo add exception type
        throw new RuntimeException("Customer Not Found");
    }

    @Override
    public ApplicationReviewDto getReviewById(UUID customerId, UUID reviewId) {
        return applicationReviewMapper.applicationReviewToDto(getReview(customerId, reviewId));
    }

    @Override
    public void pickupKit(UUID customerId, UUID reviewId) {
        applicationReviewManager.kitPickedUp(reviewId);
    }

    private ApplicationReview getReview(UUID customerId, UUID orderId){
        Optional<Driver> customerOptional = driverRepository.findById(customerId);

        if(customerOptional.isPresent()){
            Optional<ApplicationReview> applicationReviewOptional = applicationReviewRepository.findById(orderId);

            if(applicationReviewOptional.isPresent()){
                ApplicationReview applicationReview = applicationReviewOptional.get();

                // fall to exception if customer id's do not match - order not for customer
                if(applicationReview.getDriver().getId().equals(customerId)){
                    return applicationReview;
                }
            }
            throw new RuntimeException("Application Review Not Found");
        }
        throw new RuntimeException("Driver Not Found");
    }
}
