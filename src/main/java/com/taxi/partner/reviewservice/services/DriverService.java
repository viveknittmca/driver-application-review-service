package com.taxi.partner.reviewservice.services;

import com.taxi.partner.model.DriverPagedList;
import org.springframework.data.domain.Pageable;

/**
 * Created by jt on 3/7/20.
 */
public interface DriverService {

    DriverPagedList listDrivers(Pageable pageable);

}
