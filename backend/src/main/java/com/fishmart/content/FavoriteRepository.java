package com.fishmart.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {
  Optional<FavoriteEntity> findByUserIdAndTargetTypeAndTargetId(Long userId, TargetType targetType, Long targetId);
  long countByTargetTypeAndTargetId(TargetType targetType, Long targetId);
  long countByUserId(Long userId);
  Page<FavoriteEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
  Page<FavoriteEntity> findByUserIdAndTargetTypeOrderByCreatedAtDesc(Long userId, TargetType targetType, Pageable pageable);
  List<FavoriteEntity> findByUserIdAndTargetTypeAndTargetIdIn(Long userId, TargetType targetType, Collection<Long> targetIds);
  @Query("select f.targetId as targetId, count(f) as total from FavoriteEntity f where f.targetType = :type and f.targetId in :ids group by f.targetId")
  List<TargetCountView> countGrouped(@Param("type") TargetType type, @Param("ids") Collection<Long> targetIds);
}
