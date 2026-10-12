package com.fishmart.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
  Optional<LikeEntity> findByUserIdAndTargetTypeAndTargetId(Long userId, TargetType targetType, Long targetId);
  long countByTargetTypeAndTargetId(TargetType targetType, Long targetId);
  List<LikeEntity> findByUserIdAndTargetTypeAndTargetIdIn(Long userId, TargetType targetType, Collection<Long> targetIds);
  @Query("select l.targetId as targetId, count(l) as total from LikeEntity l where l.targetType = :type and l.targetId in :ids group by l.targetId")
  List<TargetCountView> countGrouped(@Param("type") TargetType type, @Param("ids") Collection<Long> targetIds);
}

