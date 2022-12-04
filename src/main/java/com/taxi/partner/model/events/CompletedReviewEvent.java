package com.taxi.partner.model.events;

import com.taxi.partner.model.ApplicationDto;

public class CompletedReviewEvent extends ReviewEvent {
    public CompletedReviewEvent(ApplicationDto applicationDto) {
        super(applicationDto);
    }
}
