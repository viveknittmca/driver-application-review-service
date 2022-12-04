package com.taxi.partner.reviewservice.domain;

/**
 * Created by vivek on 01/12/22.
 */
public enum ReviewEventEnum {
    VALIDATE_REVIEW, VALIDATION_PASSED, VALIDATION_FAILED,
    VERIFY_REVIEW, VERIFICATION_SUCCEED, VERIFICATION_PENDING, VERIFICATION_FAILED,
    CANCEL_REVIEW,
    SHIP_KIT
}
