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
package com.taxi.partner.reviewservice.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created by jt on 2019-01-26.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
public class ApplicationReviewLine extends BaseEntity {

    @Builder
    public ApplicationReviewLine(UUID id, Long version, Timestamp createdDate, Timestamp lastModifiedDate,
                                 ApplicationReview applicationReview, UUID applicationId, String phoneNumber, Integer reviewCount,
                                 Integer countVerified) {
        super(id, version, createdDate, lastModifiedDate);
        this.applicationReview = applicationReview;
        this.applicationId = applicationId;
        this.phoneNumber = phoneNumber;
        this.reviewCount = reviewCount;
        this.countVerified = countVerified;
    }

    @ManyToOne
    private ApplicationReview applicationReview;

    private UUID applicationId;
    private String phoneNumber;
    private Integer reviewCount = 0;
    private Integer countVerified = 0;
}
