package com.fishmart.guestbook;

import com.fishmart.content.ContentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestbookRepository extends JpaRepository<GuestbookMessage, Long> {
  Page<GuestbookMessage> findByStatusOrderByCreatedAtDesc(ContentStatus status, Pageable pageable);
}
