package com.damian.photogram.customers.profile;

public class ProfileDTOMapper {
    public static ProfileDTO toProfileDTO(Profile profile) {
        return new ProfileDTO(
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
