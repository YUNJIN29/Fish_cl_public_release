package com.fishmart.diagnose;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface DiagnosisFollowUpRepository extends JpaRepository<DiagnosisFollowUp, Long> {
  List<DiagnosisFollowUp> findByDiagnoseRecordIdAndUserIdOrderByObservedAtDesc(Long diagnoseRecordId, Long userId);
  long countByDiagnoseRecordIdAndUserId(Long diagnoseRecordId, Long userId);
  Optional<DiagnosisFollowUp> findByIdAndUserId(Long id, Long userId);
  @EntityGraph(attributePaths = "diagnoseRecord")
  Page<DiagnosisFollowUp> findByUserIdOrderByObservedAtDesc(Long userId, Pageable pageable);
}
