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
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

/**
 * Created by vivek on 01/12/22.
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
public class Review extends BaseEntity {

    private UUID applicationId;

    @Enumerated(EnumType.STRING)
    private ReviewStatusEnum reviewStatus = ReviewStatusEnum.NEW;

    @Builder
    public Review(UUID id, Long version, Timestamp createdDate, Timestamp lastModifiedDate, UUID applicationId) {
        super(id, version, createdDate, lastModifiedDate);
        this.applicationId = applicationId;
//      this.reviewStatusCallbackUrl = reviewStatusCallbackUrl;
    }
}
