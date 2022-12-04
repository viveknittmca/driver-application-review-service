package com.taxi.partner.model.events;

import com.taxi.partner.model.ApplicationDto;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
@Builder
public class ReviewEvent implements Serializable {

    static final long serialVersionUID = 1_478_663_319_989_504_170L;
    private final ApplicationDto applicationDto;
}
