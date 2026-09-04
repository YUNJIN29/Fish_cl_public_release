package com.fishmart.content;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  Page<Comment> findByTargetTypeAndTargetIdAndStatusOrderByIdDesc(TargetType targetType, Long targetId, ContentStatus status, Pageable pageable);
  Page<Comment> findByStatusOrderByIdDesc(ContentStatus status, Pageable pageable);
}

