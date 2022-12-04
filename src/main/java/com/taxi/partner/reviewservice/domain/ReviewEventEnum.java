package com.taxi.partner.reviewservice.domain;

/**
 * Created by jt on 11/29/19.
 */
public enum ReviewEventEnum {
    VALIDATE_REVIEW, VALIDATION_PASSED, VALIDATION_FAILED,
    VERIFY_REVIEW, VERIFICATION_SUCCEED, VERIFICATION_PENDING, VERIFICATION_FAILED,
    CANCEL_REVIEW,
    SHIP_KIT
}
