package com.taxi.partner.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Created by vivek on 01/12/22.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationFailureEvent {
    private UUID reviewId;
}
