package com.taxi.partner.reviewservice.web.controllers;

import com.taxi.partner.reviewservice.services.DriverService;
import com.taxi.partner.model.DriverPagedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jt on 3/7/20.
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/drivers/")
@RestController
public class DriverController {

    private static final Integer DEFAULT_PAGE_NUMBER = 0;
    private static final Integer DEFAULT_PAGE_SIZE = 25;

    private final DriverService driverService;

    @GetMapping
    public DriverPagedList listDrivers(@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                       @RequestParam(value = "pageSize", required = false) Integer pageSize){

        if (pageNumber == null || pageNumber < 0){
            pageNumber = DEFAULT_PAGE_NUMBER;
        }

        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        return driverService.listDrivers(PageRequest.of(pageNumber, pageSize));
    }
}
