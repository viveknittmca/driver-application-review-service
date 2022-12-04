package com.taxi.partner.model.events;

import com.taxi.partner.model.ApplicationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationEvent implements Serializable {

    static final long serialVersionUID = -7_562_421_079_300_824_242L;
    private ApplicationDto applicationDto;
}


