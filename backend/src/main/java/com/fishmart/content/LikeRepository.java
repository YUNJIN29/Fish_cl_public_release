package com.fishmart.content;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
  Optional<LikeEntity> findByUserIdAndTargetTypeAndTargetId(Long userId, TargetType targetType, Long targetId);
  long countByTargetTypeAndTargetId(TargetType targetType, Long targetId);
}

