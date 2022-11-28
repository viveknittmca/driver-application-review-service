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

import com.taxi.partner.reviewservice.services.ApplicationReviewService;
import com.taxi.partner.model.ApplicationReviewDto;
import com.taxi.partner.model.ApplicationReviewPagedList;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/v1/drivers/{driverId}/")
@RestController
public class ApplicationReviewController {

    private static final Integer DEFAULT_PAGE_NUMBER = 0;
    private static final Integer DEFAULT_PAGE_SIZE = 25;

    private final ApplicationReviewService applicationReviewService;

    public ApplicationReviewController(ApplicationReviewService applicationReviewService) {
        this.applicationReviewService = applicationReviewService;
    }

    @GetMapping("reviews")
    public ApplicationReviewPagedList listReviews(@PathVariable("driverId") UUID driverId,
                                                  @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                                  @RequestParam(value = "pageSize", required = false) Integer pageSize){

        if (pageNumber == null || pageNumber < 0){
            pageNumber = DEFAULT_PAGE_NUMBER;
        }

        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        return applicationReviewService.listReviews(driverId, PageRequest.of(pageNumber, pageSize));
    }

    @PostMapping("reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationReviewDto placeReview(@PathVariable("driverId") UUID driverId, @RequestBody ApplicationReviewDto applicationReviewDto){
        return applicationReviewService.placeReview(driverId, applicationReviewDto);
    }

    @GetMapping("reviews/{reviewId}")
    public ApplicationReviewDto getReview(@PathVariable("driverId") UUID driverId, @PathVariable("reviewId") UUID reviewId){
        return applicationReviewService.getReviewById(driverId, reviewId);
    }

    @PutMapping("/reviews/{reviewId}/pickup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pickupKit(@PathVariable("driverId") UUID customerId, @PathVariable("reviewId") UUID reviewId){
        applicationReviewService.pickupKit(customerId, reviewId);
    }
}
