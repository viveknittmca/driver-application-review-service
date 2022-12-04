package com.taxi.partner.model;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by vivek on 01/12/22.
 */
public class DriverPagedList extends PageImpl<DriverDto> {
    public DriverPagedList(List<DriverDto> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public DriverPagedList(List<DriverDto> content) {
        super(content);
    }
}
