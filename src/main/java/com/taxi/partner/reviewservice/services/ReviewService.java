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


import com.taxi.partner.model.ReviewDto;
import com.taxi.partner.model.ReviewPagedList;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {
    ReviewPagedList listReviews(Pageable pageable);

    ReviewDto placeReview(ReviewDto reviewDto);

    ReviewDto getReviewById(UUID reviewId);

    void pickupKit(UUID customerId, UUID reviewId);
}
