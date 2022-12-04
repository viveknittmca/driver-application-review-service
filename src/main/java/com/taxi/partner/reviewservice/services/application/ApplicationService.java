package com.taxi.partner.reviewservice.services.application;

import com.taxi.partner.model.ApplicationDto;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationService {

    Optional<ApplicationDto> getApplicationById(UUID uuid);

    Optional<ApplicationDto> getApplicationByPhoneNumber(String phoneNumber);
}
