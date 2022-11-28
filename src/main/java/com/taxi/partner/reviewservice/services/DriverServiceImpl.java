package com.taxi.partner.reviewservice.services;

import com.taxi.partner.reviewservice.domain.Driver;
import com.taxi.partner.reviewservice.repositories.DriverRepository;
import com.taxi.partner.reviewservice.web.mappers.DriverMapper;
import com.taxi.partner.model.DriverPagedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Created by jt on 3/7/20.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

    @Override
    public DriverPagedList listDrivers(Pageable pageable) {

        Page<Driver> customerPage = driverRepository.findAll(pageable);

        return new DriverPagedList(customerPage
                        .stream()
                        .map(driverMapper::driverToDto)
                        .collect(Collectors.toList()),
                    PageRequest.of(customerPage.getPageable().getPageNumber(),
                        customerPage.getPageable().getPageSize()),
                        customerPage.getTotalElements());
    }
}
