package com.fishmart.upload;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UploadResourceRepository extends JpaRepository<UploadResource, String> {
  @Query("""
      select r from UploadResource r
      where (:q is null or lower(r.originalName) like lower(concat('%', :q, '%')))
        and (:kind is null or (:kind = 'IMAGE' and r.contentType like 'image/%') or (:kind = 'DOCUMENT' and r.contentType = 'application/pdf'))
      """)
  Page<UploadResource> search(@Param("q") String q, @Param("kind") String kind, Pageable pageable);
}
