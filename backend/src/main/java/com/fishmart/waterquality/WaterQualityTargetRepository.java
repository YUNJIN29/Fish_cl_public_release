package com.fishmart.waterquality;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WaterQualityTargetRepository extends JpaRepository<WaterQualityTarget, Long> {
  List<WaterQualityTarget> findByUserIdOrderByPondName(Long userId);
  Optional<WaterQualityTarget> findByUserIdAndPondName(Long userId, String pondName);
  Optional<WaterQualityTarget> findByIdAndUserId(Long id, Long userId);
}
