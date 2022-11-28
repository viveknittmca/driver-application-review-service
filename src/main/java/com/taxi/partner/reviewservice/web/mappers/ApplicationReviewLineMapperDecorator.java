package com.taxi.partner.reviewservice.web.mappers;

import com.taxi.partner.reviewservice.domain.ApplicationReviewLine;
import com.taxi.partner.reviewservice.services.beer.ApplicationService;
import com.taxi.partner.model.ApplicationDto;
import com.taxi.partner.model.ApplicationReviewLineDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Optional;

/**
 * Created by jt on 2019-06-09.
 */
public abstract class ApplicationReviewLineMapperDecorator implements ApplicationReviewLineMapper {

    private ApplicationService applicationService;
    private ApplicationReviewLineMapper applicationReviewLineMapper;

    @Autowired
    public void setApplicationService(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Autowired
    @Qualifier("delegate")
    public void setApplicationReviewLineMapper(ApplicationReviewLineMapper applicationReviewLineMapper) {
        this.applicationReviewLineMapper = applicationReviewLineMapper;
    }

    @Override
    public ApplicationReviewLineDto applicationReviewLineToDto(ApplicationReviewLine line) {
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
