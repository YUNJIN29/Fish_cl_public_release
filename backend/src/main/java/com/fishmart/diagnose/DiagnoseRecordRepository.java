package com.fishmart.diagnose;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;

public interface DiagnoseRecordRepository extends JpaRepository<DiagnoseRecord, Long> {
  Page<DiagnoseRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
  long countByUserId(Long userId);
  List<DiagnoseRecord> findByCreatedAtBefore(Instant cutoff);
  @EntityGraph(attributePaths = "user")
  Optional<DiagnoseRecord> findByImageUrl(String imageUrl);
  Optional<DiagnoseRecord> findByIdAndUserId(Long id, Long userId);
}
