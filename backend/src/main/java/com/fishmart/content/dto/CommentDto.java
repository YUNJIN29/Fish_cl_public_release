package com.fishmart.content.dto;

import com.fishmart.content.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class CommentDto {
  private Long id;
  private String content;
  private Instant createdAt;
  private Long userId;
  private String nickname;

  public static CommentDto from(Comment c) {
    return new CommentDto(
        c.getId(),
        c.getContent(),
        c.getCreatedAt(),
        c.getUser().getId(),
        c.getUser().getNickname()
    );
  }
}

