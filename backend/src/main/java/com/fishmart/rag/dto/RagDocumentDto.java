package com.fishmart.rag.dto;

import com.fishmart.rag.RagDocument;
import lombok.Data;

@Data
public class RagDocumentDto {
  private Long id;
  private String title;
  private String sourceName;
  private String sourceUrl;
  private String sourceType;
  private String language;
  private String category;
  private String tags;
  private String summary;
  private String snippet;

  public static RagDocumentDto from(RagDocument doc) {
    RagDocumentDto dto = new RagDocumentDto();
    dto.id = doc.getId();
    dto.title = doc.getTitle();
    dto.sourceName = doc.getSourceName();
    dto.sourceUrl = doc.getSourceUrl();
    dto.sourceType = doc.getSourceType();
    dto.language = doc.getLanguage();
    dto.category = doc.getCategory();
    dto.tags = doc.getTags();
    dto.summary = doc.getSummary();
    dto.snippet = buildSnippet(doc.getContent());
    return dto;
  }

  private static String buildSnippet(String content) {
    if (content == null) return "";
    String text = content.replaceAll("\\s+", " ").trim();
    return text.length() > 220 ? text.substring(0, 220) + "..." : text;
  }
}
