package com.fishmart.visit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface DailyVisitRepository extends JpaRepository<DailyVisit, Long> {
  Optional<DailyVisit> findByVisitDateAndIpAddress(LocalDate visitDate, String ipAddress);
  long countByVisitDate(LocalDate visitDate);
  long deleteByVisitDateBefore(LocalDate cutoff);

  @Query("select v.visitDate, sum(v.visitCount), count(distinct v.ipAddress) from DailyVisit v where v.visitDate between :start and :end group by v.visitDate order by v.visitDate")
  List<Object[]> trafficBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

  @Query("select count(distinct v.ipAddress) from DailyVisit v")
  long countAllUniqueIps();

  @Query("select count(distinct v.ipAddress) from DailyVisit v where v.visitDate between :start and :end")
  long countUniqueIpsBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

  @Query("select v.ipAddress, max(v.location), sum(v.visitCount) from DailyVisit v where v.visitDate between :start and :end group by v.ipAddress order by sum(v.visitCount) desc")
  List<Object[]> monthlyRanking(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
