package com.damian.photogram.common.exception;

public class Exceptions {
    public static class AUTH {
        public static final String NOT_ADMIN = "You are not an admin.";
        public static final String BAD_CREDENTIALS = "Bad credentials.";
    }

    public static class FRIEND_LIST {
        public static final String ALREADY_EXISTS = "Friend already exists.";
        public static final String ACCESS_FORBIDDEN = "You cannot access this follower.";
        public static final String NOT_FOUND = "Friend not found.";
        public static final String MAX_FRIENDS = "You have reached the maximum number of friends.";
    }


    public static class CUSTOMER {
        public static final String DISABLED = "Customer is disabled.";
        public static final String EMAIL_TAKEN = "Email is already taken.";
        public static final String NOT_FOUND = "Customer not found.";
    }

    public static class EXPENSES {
        public static final String NOT_FOUND = "Expense not found.";
    }

    public static class GROUP {
        public static final String NOT_FOUND = "Group not found.";
        public static final String NOT_OWNER = "You are not the owner of this group.";
        public static final String ACCESS_FORBIDDEN = "You are not authorized to access this group.";
        public static final String NOT_MEMBER = "You are not members of this group.";
    }

    public static class SETTINGS {
        public static final String NOT_FOUND = "Group not found.";
        public static final String NOT_OWNER = "You are not the owner of this setting.";
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
