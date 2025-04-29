package ongi.ongibe.domain.auth.dto;

import lombok.Getter;

public class KakaoDTO {

    @Getter
    public static class OAuthToken {
        private String token_type;
        private String access_token;
        private String id_token;
        private int expires_in;
        private String refresh_token;
        private int refresh_token_expires_in;
        private String scope;
    }

    @Getter
    public static class KakaoProfile {
        private Long id;
        private String connected_at;
        private Properties properties;
        private KakaoAccount kakao_account;

        @Getter
        public static class Properties {
            private String nickname;
        }

        @Getter
        public static class KakaoAccount {
            private String email;
            private Boolean is_email_verified;
            private Boolean has_email;
            private Boolean profile_nickname_needs_agreement;
            private Boolean email_needs_agreement;
            private Boolean is_email_valid;
            private Profile profile;

            @Getter
            public static class Profile {
                private String nickname;
                private Boolean is_default_nickname;
            }
        }
    }
}