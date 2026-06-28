package com.chungbazi.server.domain.user.api.dto;

import lombok.Builder;

@Builder
public record UserNameRequest(
        String name
) {
}
