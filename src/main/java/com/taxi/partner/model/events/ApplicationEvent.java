package com.taxi.partner.model.events;

import com.taxi.partner.model.ApplicationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by jt on 2019-07-21.
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ApplicationEvent implements Serializable {

    static final long serialVersionUID = -5781515597148163111L;

    private ApplicationDto applicationDto;
}
