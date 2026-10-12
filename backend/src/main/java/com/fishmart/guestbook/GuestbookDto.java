package com.fishmart.guestbook;

import java.time.Instant;

public record GuestbookDto(Long id, String author, String content, String status, String reply, Instant createdAt) {
  public static GuestbookDto from(GuestbookMessage item, boolean includeStatus) {
    String author = item.getAuthorName();
    return new GuestbookDto(item.getId(), author, item.getContent(), includeStatus ? item.getStatus().name() : null,
        item.getAdminReply(), item.getCreatedAt());
  }
}
