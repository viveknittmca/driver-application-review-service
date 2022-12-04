package com.taxi.partner.model.events;

import com.taxi.partner.model.ApplicationDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NewApplicationEvent extends ApplicationEvent {
    public NewApplicationEvent(ApplicationDto applicationDto) {
        super(applicationDto);
    }
}
