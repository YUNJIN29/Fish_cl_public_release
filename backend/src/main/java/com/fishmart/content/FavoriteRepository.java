package com.fishmart.content;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {
  Optional<FavoriteEntity> findByUserIdAndTargetTypeAndTargetId(Long userId, TargetType targetType, Long targetId);
  long countByTargetTypeAndTargetId(TargetType targetType, Long targetId);
  long countByUserId(Long userId);
}
