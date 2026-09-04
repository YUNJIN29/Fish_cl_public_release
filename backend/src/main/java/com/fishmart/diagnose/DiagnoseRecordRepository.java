package com.fishmart.diagnose;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnoseRecordRepository extends JpaRepository<DiagnoseRecord, Long> {
  Page<DiagnoseRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
  long countByUserId(Long userId);
}
