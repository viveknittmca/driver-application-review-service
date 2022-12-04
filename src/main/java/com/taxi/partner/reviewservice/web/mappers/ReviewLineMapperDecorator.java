package com.taxi.partner.reviewservice.web.mappers;

import com.taxi.partner.reviewservice.domain.ReviewLine;
import com.taxi.partner.reviewservice.services.application.ApplicationService;
import com.taxi.partner.model.ApplicationDto;
import com.taxi.partner.model.ApplicationReviewLineDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Optional;

/**
 * Created by vivek on 01/12/22.
 */
public abstract class ReviewLineMapperDecorator implements ReviewLineMapper {

    private ApplicationService applicationService;
    private ReviewLineMapper applicationReviewLineMapper;

    @Autowired
    public void setApplicationService(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Autowired
    @Qualifier("delegate")
    public void setApplicationReviewLineMapper(ReviewLineMapper applicationReviewLineMapper) {
        this.applicationReviewLineMapper = applicationReviewLineMapper;
    }

    @Override
    public ApplicationReviewLineDto applicationReviewLineToDto(ReviewLine line) {
        ApplicationReviewLineDto orderLineDto = applicationReviewLineMapper.applicationReviewLineToDto(line);
        Optional<ApplicationDto> beerDtoOptional = applicationService.getApplicationByPhoneNumber(line.getPhoneNumber());

        beerDtoOptional.ifPresent(beerDto -> {
            orderLineDto.setApplicationName(beerDto.getApplicationName());
            orderLineDto.setApplicationType(beerDto.getApplicationType());
            orderLineDto.setApplicationReviewId(beerDto.getId());
        });

        return orderLineDto;
    }
}
