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

package com.taxi.partner.reviewservice.web.mappers;

import com.taxi.partner.reviewservice.domain.ApplicationReview;
import com.taxi.partner.model.ApplicationReviewDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {DateMapper.class, ApplicationReviewLineMapper.class})
public interface ApplicationReviewMapper {

    @Mapping(target = "driverId", source = "driver.id")
    ApplicationReviewDto applicationReviewToDto(ApplicationReview applicationReview);

    ApplicationReview dtoToApplicationReview(ApplicationReviewDto dto);
}
