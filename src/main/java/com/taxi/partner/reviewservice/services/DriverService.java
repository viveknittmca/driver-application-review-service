package com.taxi.partner.reviewservice.services;

import com.taxi.partner.model.DriverPagedList;
import org.springframework.data.domain.Pageable;

/**
 * Created by vivek on 01/12/22.
 */
public interface DriverService {

    DriverPagedList listDrivers(Pageable pageable);

}
