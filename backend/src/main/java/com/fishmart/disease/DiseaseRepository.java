package com.fishmart.disease;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {
  boolean existsByName(String name);

  @Query("""
      SELECT d FROM Disease d
      WHERE (:q IS NULL OR :q = ''
             OR d.name LIKE %:q%
             OR d.affectedSpecies LIKE %:q%
             OR d.symptomsSummary LIKE %:q%
             OR d.diseaseType LIKE %:q%)
      AND (:type IS NULL OR :type = '' OR d.diseaseType = :type)
      """)
  Page<Disease> search(@Param("q") String q, @Param("type") String type, Pageable pageable);
}
