package com.fishmart.game;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface GameScoreRepository extends JpaRepository<GameScore,Long>{Optional<GameScore> findByGameKeyAndIpAddress(String gameKey,String ipAddress);List<GameScore> findTop10ByGameKeyOrderByScoreDescUpdatedAtAsc(String gameKey);}
