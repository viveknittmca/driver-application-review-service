package com.taxi.partner.reviewservice.web.mappers;

import com.taxi.partner.reviewservice.domain.ReviewLine;
import com.taxi.partner.model.ApplicationReviewLineDto;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

@Mapper(uses = {DateMapper.class})
@DecoratedWith(ReviewLineMapperDecorator.class)
public interface ReviewLineMapper {
    ApplicationReviewLineDto applicationReviewLineToDto(ReviewLine line);

    ReviewLine dtoToApplicationReviewLine(ApplicationReviewLineDto dto);
}
