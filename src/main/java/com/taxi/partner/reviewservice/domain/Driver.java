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
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

/**
 * Created by vivek on 01/12/22.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Driver extends BaseEntity {

    @Builder
    public Driver(UUID id, Long version, Timestamp createdDate, Timestamp lastModifiedDate, String driverName,
                  UUID apiKey, Set<Review> reviews) {
        super(id, version, createdDate, lastModifiedDate);
        this.driverName = driverName;
        this.apiKey = apiKey;
        this.reviews = reviews;
    }

    private String driverName;

    @Type(type="org.hibernate.type.UUIDCharType")
    @Column(length = 36, columnDefinition = "varchar(36)")
    private UUID apiKey;

    @OneToMany(mappedBy = "driver")
    private Set<Review> reviews;

}
