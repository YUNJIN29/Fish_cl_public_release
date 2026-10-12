package com.fishmart.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.time.Instant;

public interface AquacultureTaskRepository extends JpaRepository<AquacultureTask, Long> {
  @Query("""
      select t from AquacultureTask t
      where t.user.id = :userId
        and (:status = 'ALL'
          or (:status = 'OPEN' and t.completedAt is null)
          or (:status = 'COMPLETED' and t.completedAt is not null))
        and (:status <> 'OPEN' or :scope = 'ALL'
          or (:scope = 'OVERDUE' and t.dueAt < :now)
          or (:scope = 'DUE_SOON' and t.dueAt >= :now and t.dueAt < :soon)
          or (:scope = 'RECURRING' and t.repeatIntervalDays is not null))
        and (:category = '' or t.category = :category)
        and (:pondName = '' or t.pondName = :pondName)
        and (:keyword = '' or lower(t.title) like lower(concat('%', :keyword, '%'))
          or lower(coalesce(t.notes, '')) like lower(concat('%', :keyword, '%')))
      """)
  Page<AquacultureTask> search(@Param("userId") Long userId,
                               @Param("status") String status,
                               @Param("scope") String scope,
                               @Param("category") String category,
                               @Param("pondName") String pondName,
                               @Param("keyword") String keyword,
                               @Param("now") Instant now,
                               @Param("soon") Instant soon,
                               Pageable pageable);

  @Query("select distinct t.pondName from AquacultureTask t where t.user.id = :userId and t.pondName is not null and t.pondName <> '' order by t.pondName")
  List<String> findDistinctPondNames(@Param("userId") Long userId);

  Page<AquacultureTask> findByUserIdAndCompletedAtIsNullOrderByDueAtAsc(Long userId, Pageable pageable);
  List<AquacultureTask> findByUserIdAndCompletedAtIsNullOrderByDueAtAsc(Long userId);
  Page<AquacultureTask> findByUserIdAndCompletedAtIsNullAndDueAtBeforeOrderByDueAtAsc(Long userId, Instant dueAt, Pageable pageable);
  Page<AquacultureTask> findByUserIdAndCompletedAtIsNullAndDueAtGreaterThanEqualAndDueAtBeforeOrderByDueAtAsc(Long userId, Instant from, Instant to, Pageable pageable);
  Page<AquacultureTask> findByUserIdAndCompletedAtIsNullAndRepeatIntervalDaysIsNotNullOrderByDueAtAsc(Long userId, Pageable pageable);
  Page<AquacultureTask> findByUserIdAndCompletedAtIsNotNullOrderByCompletedAtDesc(Long userId, Pageable pageable);
  Page<AquacultureTask> findByUserIdOrderByDueAtDesc(Long userId, Pageable pageable);
  Optional<AquacultureTask> findByIdAndUserId(Long id, Long userId);
  boolean existsByUserIdAndCompletedAtIsNullAndPondNameAndTitle(Long userId, String pondName, String title);
  long countByUserIdAndCompletedAtIsNull(Long userId);
  long countByUserIdAndCompletedAtIsNullAndDueAtBefore(Long userId, Instant dueAt);
  long countByUserIdAndCompletedAtIsNullAndDueAtGreaterThanEqualAndDueAtBefore(Long userId, Instant from, Instant to);
  long countByUserIdAndCompletedAtIsNullAndRepeatIntervalDaysIsNotNull(Long userId);
}
