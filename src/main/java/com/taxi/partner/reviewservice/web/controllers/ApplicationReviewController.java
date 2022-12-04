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

package com.taxi.partner.reviewservice.web.controllers;

import com.taxi.partner.reviewservice.services.ReviewService;
import com.taxi.partner.model.ReviewDto;
import com.taxi.partner.model.ReviewPagedList;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/v1/")
@RestController
public class ApplicationReviewController {

    private static final Integer DEFAULT_PAGE_NUMBER = 0;
    private static final Integer DEFAULT_PAGE_SIZE = 25;

    private final ReviewService reviewService;

    public ApplicationReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("reviews")
    public ReviewPagedList listReviews(@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                       @RequestParam(value = "pageSize", required = false) Integer pageSize){

        if (pageNumber == null || pageNumber < 0){
            pageNumber = DEFAULT_PAGE_NUMBER;
        }

        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        return reviewService.listReviews(PageRequest.of(pageNumber, pageSize));
    }

    @PostMapping("reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto placeReview(@RequestBody ReviewDto reviewDto){
        return reviewService.placeReview(reviewDto);
    }

    @GetMapping("reviews/{reviewId}")
    public ReviewDto getReviewById(@PathVariable("reviewId") UUID reviewId){
        return reviewService.getReviewById(reviewId);
    }

    @PutMapping("/reviews/{reviewId}/pickup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pickupKit(@PathVariable("driverId") UUID customerId, @PathVariable("reviewId") UUID reviewId){
        reviewService.pickupKit(customerId, reviewId);
    }
}
