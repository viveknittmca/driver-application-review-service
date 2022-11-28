package com.taxi.partner.reviewservice.web.mappers;

import com.taxi.partner.reviewservice.domain.Driver;
import com.taxi.partner.model.DriverDto;
import org.mapstruct.Mapper;

/**
 * Created by jt on 3/7/20.
 */
@Mapper(uses = {DateMapper.class})
public interface DriverMapper {
    DriverDto driverToDto(Driver driver);

    Driver dtoToDriver(Driver dto);
}
