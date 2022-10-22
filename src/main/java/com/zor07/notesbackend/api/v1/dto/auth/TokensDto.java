package com.zor07.notesbackend.api.v1.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokensDto(@JsonProperty("access_token") String accessToken,
                        @JsonProperty("refresh_token") String refreshToken) {
}
