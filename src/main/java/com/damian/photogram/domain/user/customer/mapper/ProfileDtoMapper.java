package com.damian.photogram.domain.user.customer.mapper;

import com.damian.photogram.domain.user.customer.dto.response.ProfileDto;
import com.damian.photogram.domain.user.customer.model.Profile;

public class ProfileDtoMapper {
    public static ProfileDto toProfileDto(Profile profile) {
        return new ProfileDto(
                profile.getId(),
                profile.getUsername(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhone(),
                profile.getBirthdate(),
                profile.getGender(),
                profile.getImageFilename(),
                profile.getUpdatedAt()
        );
    }
}
