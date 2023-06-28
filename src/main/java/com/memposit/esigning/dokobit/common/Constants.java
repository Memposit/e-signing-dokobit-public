package com.memposit.esigning.dokobit.common;

public final class Constants {

    private Constants() {}

    public static final class Params {

        private Params() {}

        public static final String RETURN_URL = "return_url";
        public static final String JSON = ".json";
    }

    public static final class Status {

        private Status() {}

        public static final String OK = "ok";
    }

    public static final class MobileId {

        private MobileId() {}

        public static final String MOBILE_ID_PHONE = "phone";
        public static final String MOBILE_ID_CODE = "code";
    }

    public static final class FileType {

        private FileType() {}

        public static final String TYPE = "type";
        public static final String PDF = "pdf";
    }

    public static final class UserInfo {

        private UserInfo() {}

        public static final String LANGUAGE = "language";
        public static final String COUNTRY = "country";
        public static final String DEFAULT_LANGUAGE = "ET";
        public static final String DEFAULT_COUNTRY = "EE";
    }

    public static final class FileMetaData {

        private FileMetaData() {}

        public static final String CONTACT = "pdf[contact]";
        public static final String PDF_FILES = "pdf[files]";
        public static final String NAME = "[name]";
        public static final String CONTENT = "[content]";
        public static final String DIGEST = "[digest]";
        public static final String SIGNING = "signing";
    }

    public static final class Auth {

        private Auth() {}

        public static final String AUTH_REDIRECT = "auth/redirect";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String URL = "url";
        public static final String STATUS = "status";
        public static final String CODE = "code";
        public static final String TOKEN = "token";
    }

}
