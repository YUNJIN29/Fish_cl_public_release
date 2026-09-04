package com.fishmart.content;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long> {
  @Query("""
      select a from Article a
      where (:q is null or :q = '' or lower(a.title) like lower(concat('%', :q, '%')))
        and a.status = :status
      """)
  Page<Article> search(@Param("q") String q, @Param("status") ContentStatus status, Pageable pageable);

  @Query("""
      select a from Article a
      where (:q is null or :q = '' or lower(a.title) like lower(concat('%', :q, '%')))
      """)
  Page<Article> searchAll(@Param("q") String q, Pageable pageable);
}

