package com.fishmart.content.dto;

import com.fishmart.content.Article;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ArticleDto {
  private Long id;
  private String title;
  private String coverImageUrl;
  private String content;
  private Instant createdAt;

  public static ArticleDto from(Article a, boolean withContent) {
    return new ArticleDto(a.getId(), a.getTitle(), a.getCoverImageUrl(), withContent ? a.getContent() : null, a.getCreatedAt());
  }
}

