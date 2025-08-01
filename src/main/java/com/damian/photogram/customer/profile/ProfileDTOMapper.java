package com.damian.photogram.customer.profile;

public class ProfileDTOMapper {
    public static ProfileDTO toProfileDTO(Profile profile) {
        return new ProfileDTO(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhone(),
                profile.getBirthdate(),
                profile.getGender(),
                profile.getAvatarFilename(),
                profile.getUpdatedAt()
        );
    }
}
