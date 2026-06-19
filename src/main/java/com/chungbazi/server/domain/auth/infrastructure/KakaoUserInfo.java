package com.chungbazi.server.domain.auth.infrastructure;

import com.chungbazi.server.domain.auth.domain.OAuth2UserInfo;
import com.chungbazi.server.global.common.code.exception.GeneralException;
import com.chungbazi.server.global.common.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
public class KakaoUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    @Override
    public String getProviderId() {
        Object id = attributes.get("id");
        if (id == null) {
            throw new GeneralException(ErrorStatus._KAKAO_REQUIRED_INFO_MISSING);
        }
        return String.valueOf(id);
    }

    @Override
    public String getEmail() {
        String email = (String) getKakaoAccount().get("email");

        if (email == null || email.isBlank()) {
            throw new GeneralException(ErrorStatus._KAKAO_REQUIRED_INFO_MISSING);
        }
        return email;
    }

    @Override
    public String getName() {
        String nickname = (String) getKakaoProfile().get("nickname");

        if (nickname == null || nickname.isBlank()) {
            throw new GeneralException(ErrorStatus._KAKAO_REQUIRED_INFO_MISSING);
        }
        return nickname;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getKakaoAccount() {
        Object account = attributes.get("kakao_account");
        if (account instanceof Map) {
            return (Map<String, Object>) account;
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getKakaoProfile() {
        Object profile = getKakaoAccount().get("profile");
        if (profile instanceof Map) {
            return (Map<String, Object>) profile;
        }
        return Collections.emptyMap();
    }
}
