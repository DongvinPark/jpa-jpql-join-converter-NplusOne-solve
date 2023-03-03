package com.example.teamsnstest.converter;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public interface ForTestResponse {
    Long getFeedId();
    Long getUserId();
    LocalDateTime getCreatedAt();
    LocalDateTime getModifiedAt();
    String getImageUrls();
    String getContent();
    String getUsername();
    String getProfileImageUrl();
    String getNickname();
}
