package com.damian.photogram.core.exception;


public class Exceptions {

    public static class ACCOUNT {
        public static final String BAD_CREDENTIALS = "Invalid email or password.";
        public static final String NOT_FOUND = "Account not found.";
        public static final String INVALID_PASSWORD = "Password does not match.";
        public static final String SUSPENDED = "Account has been suspended.";
        public static final String NOT_VERIFIED = "Account email is not verified.";

        public static class VERIFICATION {
            public static final String NOT_ELIGIBLE = "Account is not eligible for activation.";

            public static class TOKEN {
                public static final String NOT_FOUND = "Account token not found.";
                public static final String USED = "This token has already been used.";
                public static final String EXPIRED = "This token has expired.";
            }
        }
    }

    public static class CUSTOMER {
        public static final String EMAIL_TAKEN = "This email is already registered.";
        public static final String NOT_FOUND = "Customer not found.";

        public static class PROFILE {
            public static final String NOT_FOUND = "Profile not found.";
            public static final String UPDATE_FAILED_INVALID_FIELD = "Invalid profile field.";
            public static final String NOT_OWNER = "You are not the owner of this profile.";

            public static class IMAGE {
                public static final String NOT_FOUND = "Profile image not found.";
                public static final String TOO_LARGE = "Profile image is too large.";
            }
        }
    }

    public static class POST {
        public static final String NOT_AUTHOR = "You are not the author of this post.";
        public static final String NOT_FOUND = "Post not found.";
        public static final String ALREADY_LIKED = "You have already liked this post.";

        public static class IMAGE {
            public static final String NOT_FOUND = "Post image not found.";
            public static final String TOO_LARGE = "Post image is too large.";
        }

        public static class COMMENT {
            public static final String NOT_AUTHOR = "You are not the author of this comment.";
            public static final String NOT_FOUND = "Comment not found.";
        }

        public static class LIKE {
            public static final String NOT_FOUND = "Like not found.";
        }
    }

    public static class FOLLOW {
        public static final String NOT_FOUND = "Follow relation not found.";
        public static final String ALREADY_EXISTS = "You already follow this user.";
        public static final String SELF_FOLLOW = "You cannot follow yourself.";
        public static final String MAX_FOLLOWERS = "You have reached the maximum number of followers.";
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
        public static final String EMPTY = "Image file is empty.";
        public static final String INVALID = "Image is not valid.";
        public static final String UPLOAD_FAILED = "Image upload failed.";
        public static final String STORAGE_FAILED = "Image storage failed.";
        public static final String TYPE_NOT_DETECTED = "Image type could not be detected.";
    }

    public static class JWT {
        public static class TOKEN {
            public static final String EXPIRED = "JWT token has expired.";
            public static final String INVALID = "Invalid JWT token.";
        }
    }

    public static class COMMON {
        public static final String NOT_OWNER = "You are not allowed to access this resource.";
        public static final String NOT_FOUND = "Resource not found.";
    }

    public static class FEED {
        public static final String USER_PROFILE_NOT_FOUND = "Profile not found for this user.";
    }
}
