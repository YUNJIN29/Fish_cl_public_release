package com.fishmart.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

  @Query("""
      select p from Product p
      where (:categoryId is null or p.category.id = :categoryId)
        and (:q is null or :q = '' or lower(p.name) like lower(concat('%', :q, '%')))
        and (:tag is null or :tag = '' or p.symptomTags like concat('%', :tag, '%'))
        and (:status is null or p.status = :status)
      """)
  Page<Product> search(@Param("categoryId") Long categoryId,
                       @Param("q") String q,
                       @Param("tag") String tag,
                       @Param("status") ProductStatus status,
                       Pageable pageable);
}

