package com.taxi.partner.model;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by jt on 3/7/20.
 */
public class DriverPagedList extends PageImpl<DriverDto> {
    public DriverPagedList(List<DriverDto> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public DriverPagedList(List<DriverDto> content) {
        super(content);
    }
}
