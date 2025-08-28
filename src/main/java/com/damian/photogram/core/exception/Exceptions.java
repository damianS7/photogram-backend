package com.damian.photogram.core.exception;


public class Exceptions {

    public static class CUSTOMER {
        public static final String EMAIL_TAKEN = "Email is already taken.";
        public static final String NOT_FOUND = "Customer not found.";
    }

    public static class PROFILE {
        public static final String NOT_FOUND = "Profile not found.";
        public static final String INVALID_FIELD = "Field is invalid.";
        public static final String NOT_OWNER = "You are not the owner of this profile.";

        public static class IMAGE {
            public static final String NOT_FOUND = "Profile photo not found.";
            public static final String TOO_LARGE = "Profile photo is too large.";
        }
    }

    public static class SETTINGS {
        public static final String NOT_FOUND = "Setting not found.";
        public static final String NOT_OWNER = "You are not the owner of this setting.";
    }

    public static class IMAGE {
        public static final String NOT_FOUND = "Image not found.";
        public static final String INVALID_PATH = "Image path is invalid.";
        public static final String TOO_LARGE = "Image is too large.";
        public static final String TYPE_NOT_SUPPORTED = "Image type not supported.";
        public static final String EMPTY_FILE = "Image file is empty.";
        public static final String UPLOAD_FAILED = "Image upload failed.";
    }

    public static class JWT {
        public static final String INVALID_EMAIL = "Invalid email found in token.";
        public static final String TOKEN_EXPIRED = "Token has expired.";
        public static final String INVALID_TOKEN = "Token is invalid.";
    }

    public static class ACCOUNT {
        public static final String BAD_CREDENTIALS = "Bad credentials.";
        public static final String EMAIL_NOT_VERIFIED = "Email is not verified.";
        public static final String SUSPENDED = "Account is suspended.";
        public static final String NOT_FOUND = "Account not found.";
        public static final String NOT_FOUND_BY_EMAIL = "Account not found. Invalid email address.";

        public static class VERIFICATION {
            public static final String TOKEN_NOT_FOUND = "Token not found.";
            public static final String NOT_ELEGIBLE_FOR_ACTIVATION = "Account not elegible for activation.";
            public static final String INVALID_TOKEN = "Token is invalid.";
            public static final String EXPIRED_TOKEN = "Token has expired.";
            public static final String TOKEN_USED = "Token has already been used.";
        }
    }

    public static class POSTS {
        public static final String NOT_AUTHOR = "You are not the author of this post.";
        public static final String NOT_FOUND = "Post not found.";
        public static final String ALREADY_LIKED = "Post already liked.";

        public static class IMAGE {
            public static final String NOT_FOUND = "Post photo not found.";
            public static final String TOO_LARGE = "Post photo is too large.";
        }
    }

    public static class COMMENT {
        public static final String NOT_AUTHOR = "You are not the author of this comment.";
    }

    public static class LIKE {
        public static final String NOT_FOUND = "Like not found.";
    }

    public static class FOLLOW {
        public static final String ALREADY_EXISTS = "Follower already exists.";
        public static final String SELF_FOLLOW = "You cannot follow yourself.";
        public static final String NOT_FOUND = "Follower not found.";
        public static final String MAX_FOLLOWERS = "You have reached the maximum number of followers.";
    }


}
