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

import com.taxi.partner.reviewservice.domain.Review;
import com.taxi.partner.reviewservice.repositories.ReviewRepository;
import com.taxi.partner.reviewservice.repositories.DriverRepository;
import com.taxi.partner.reviewservice.web.mappers.ReviewMapper;
import com.taxi.partner.model.ReviewDto;
import com.taxi.partner.model.ReviewPagedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final ReviewManager reviewManager;

    @Override
    public ReviewPagedList listReviews(Pageable pageable) {
        //Optional<Driver> customerOptional = driverRepository.findById(customerId);

//        if (customerOptional.isPresent()) {
//            Page<Review> applicationReviewPage =
//                    reviewRepository.findAllByDriver(customerOptional.get(), pageable);
//
//            return new ApplicationReviewPagedList(applicationReviewPage
//                    .stream()
//                    .map(reviewMapper::applicationReviewToDto)
//                    .collect(Collectors.toList()), PageRequest.of(
//                    applicationReviewPage.getPageable().getPageNumber(),
//                    applicationReviewPage.getPageable().getPageSize()),
//                    applicationReviewPage.getTotalElements());
//        } else {
            return null;
     //   }
    }

    @Transactional
    @Override
    public ReviewDto placeReview(ReviewDto reviewDto) {
        return null;
//        if (driverOptional.isPresent()) {
//            Review review = reviewMapper.dtoToApplicationReview(reviewDto);
//            review.setId(null); //should not be set by outside client
//            review.setReviewStatus(ReviewStatusEnum.NEW);
//            Review savedReview = reviewManager.newReview(review);
//
//            log.debug("Saved Application Review: " + review.getId());
//
//            return reviewMapper.applicationReviewToDto(savedReview);
//        }
//        //todo add exception type
//        throw new RuntimeException("Customer Not Found");
    }

    @Override
    public ReviewDto getReviewById(UUID reviewId) {
        return reviewMapper.applicationReviewToDto(getReview(reviewId));
    }

    @Override
    public void pickupKit(UUID customerId, UUID reviewId) {
        reviewManager.kitPickedUp(reviewId);
    }

    private Review getReview(UUID orderId){
//        Optional<Driver> customerOptional = driverRepository.findById(customerId);
//
//        if(customerOptional.isPresent()){
//            Optional<Review> applicationReviewOptional = reviewRepository.findById(orderId);
//
//            if(applicationReviewOptional.isPresent()){
//                Review review = applicationReviewOptional.get();
//
//
//            }
//            throw new RuntimeException("Application Review Not Found");
//        }
//        throw new RuntimeException("Driver Not Found");
        return null;
    }
}
