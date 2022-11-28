package com.taxi.partner.reviewservice.web.mappers;

import com.taxi.partner.reviewservice.domain.ApplicationReviewLine;
import com.taxi.partner.model.ApplicationReviewLineDto;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

@Mapper(uses = {DateMapper.class})
@DecoratedWith(ApplicationReviewLineMapperDecorator.class)
public interface ApplicationReviewLineMapper {
    ApplicationReviewLineDto applicationReviewLineToDto(ApplicationReviewLine line);

    ApplicationReviewLine dtoToApplicationReviewLine(ApplicationReviewLineDto dto);
}
