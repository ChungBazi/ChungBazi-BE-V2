package com.chungbazi.server.domain.auth.domain;

public interface OAuth2UserInfo {
    String getProviderId();
    String getEmail();
    String getName ();
}
