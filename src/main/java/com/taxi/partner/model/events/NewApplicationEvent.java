package com.taxi.partner.model.events;

import com.taxi.partner.model.ApplicationDto;
import lombok.NoArgsConstructor;

/**
 * Created by jt on 2019-07-21.
 */
@NoArgsConstructor
public class NewApplicationEvent extends ApplicationEvent {

    public NewApplicationEvent(ApplicationDto applicationDto) {
        super(applicationDto);
    }
}
