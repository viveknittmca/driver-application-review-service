package com.taxi.partner.model.events;

import com.taxi.partner.model.ReviewDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by jt on 11/30/19.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateReviewRequest {

    private ReviewDto applicationReview;
}
