package com.fishmart.content.dto;

import com.fishmart.content.Post;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class PostDto {
  private Long id;
  private String title;
  private String content;
  private Instant createdAt;
  private Long userId;
  private String nickname;

  public static PostDto from(Post p, boolean withContent) {
    return new PostDto(
        p.getId(),
        p.getTitle(),
        withContent ? p.getContent() : null,
        p.getCreatedAt(),
        p.getUser().getId(),
        p.getUser().getNickname()
    );
  }
}

