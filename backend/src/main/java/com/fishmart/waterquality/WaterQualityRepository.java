package com.fishmart.waterquality;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WaterQualityRepository extends JpaRepository<WaterQualityRecord, Long> {
  Page<WaterQualityRecord> findByUserIdOrderByObservedAtDesc(Long userId, Pageable pageable);
  List<WaterQualityRecord> findByUserIdOrderByObservedAtDesc(Long userId);
  List<WaterQualityRecord> findByUserIdAndPondNameOrderByObservedAtDesc(Long userId, String pondName);
  Optional<WaterQualityRecord> findByIdAndUserId(Long id, Long userId);

  @Query("select distinct r.pondName from WaterQualityRecord r where r.user.id = :userId order by r.pondName")
  List<String> findPondNames(@Param("userId") Long userId);

  List<WaterQualityRecord> findByUserIdAndPondNameAndObservedAtAfterOrderByObservedAtAsc(
      Long userId, String pondName, Instant observedAfter, Pageable pageable);
}
