package com.taxi.partner.model.events;

import com.taxi.partner.model.ApplicationReviewDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by jt on 12/3/19.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyReviewResult {
    private ApplicationReviewDto applicationReviewDto;
    private Boolean verificationError = false;
    private Boolean pendingDocuments = false;
}
