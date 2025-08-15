package com.damian.photogram.common.exception;

public class Exceptions {
    public static class ACCOUNT {
        public static final String NOT_ADMIN = "You are not an admin.";
        public static final String BAD_CREDENTIALS = "Bad credentials.";
        public static final String EMAIL_NOT_VERIFIED = "Email is not verified.";
        public static final String SUSPENDED = "Account is suspended.";
        public static final String NOT_FOUND = "Account not found.";
        public static final String NOT_FOUND_BY_EMAIL = "Account not found. Invalid email.";
    }

    public static class ACCOUNT_ACTIVATION {
        public static final String NOT_ELEGIBLE_FOR_ACTIVATION = "Account not elegible for activation.";
        public static final String INVALID_TOKEN = "Token is invalid.";
        public static final String EXPIRED_TOKEN = "Token has expired.";
    }

    public static class ACCOUNT_TOKEN {
        public static final String NOT_FOUND = "Token not found.";
    }

    public static class POSTS {
        public static final String ACCESS_FORBIDDEN = "You cannot access this post.";
        public static final String NOT_FOUND = "Post not found.";
    }

    public static class LIKES {
        public static final String ACCESS_FORBIDDEN = "You cannot access this like.";
        public static final String NOT_FOUND = "Like not found.";
    }

    public static class FOLLOW {
        public static final String ALREADY_EXISTS = "Follower already exists.";
        public static final String ACCESS_FORBIDDEN = "You cannot access this follow.";
        public static final String SELF_FOLLOW = "You cannot follow yourself.";
        public static final String NOT_FOUND = "Follower not found.";
        public static final String MAX_FOLLOWERS = "You have reached the maximum number of followers.";
    }


    public static class CUSTOMER {
        public static final String DISABLED = "Customer is disabled.";
        public static final String EMAIL_TAKEN = "Email is already taken.";
        public static final String NOT_FOUND = "Customer not found.";
    }

    public static class SETTINGS {
        public static final String NOT_FOUND = "Group not found.";
        public static final String NOT_OWNER = "You are not the owner of this settings.";
    }

    public static class PROFILE {
        public static final String NOT_FOUND = "Profile not found.";
        public static final String INVALID_FIELD = "Field is invalid.";
        public static final String ACCESS_FORBIDDEN = "You are not authorized to access this profile.";

        public static class IMAGE {
            public static final String NOT_FOUND = "Profile photo not found.";
            public static final String FILE_SIZE_LIMIT = "Profile photo is too large.";
            public static final String ONLY_IMAGES_ALLOWED = "Profile photo must be an image.";
            public static final String EMPTY_FILE = "File is empty.";
            public static final String UPLOAD_FAILED = "Profile photo upload failed.";
        }
    }
}
