package com.fishmart.content;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
  @Query("""
      select p from Post p
      where (:q is null or :q = '' or lower(p.title) like lower(concat('%', :q, '%')))
        and p.status = :status
      """)
  Page<Post> search(@Param("q") String q, @Param("status") ContentStatus status, Pageable pageable);

  @Query("""
      select p from Post p
      where (:q is null or :q = '' or lower(p.title) like lower(concat('%', :q, '%')))
      """)
  Page<Post> searchAll(@Param("q") String q, Pageable pageable);

  Page<Post> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
  long countByUserId(Long userId);
}
