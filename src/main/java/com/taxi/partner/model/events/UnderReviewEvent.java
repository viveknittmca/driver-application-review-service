package com.taxi.partner.model.events;

import com.taxi.partner.model.ApplicationDto;

public class UnderReviewEvent extends ReviewEvent {
    public UnderReviewEvent(ApplicationDto applicationDto) {
        super(applicationDto);
    }
}
