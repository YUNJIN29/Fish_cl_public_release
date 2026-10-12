package com.fishmart.game;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface GameScoreRepository extends JpaRepository<GameScore,Long>{Optional<GameScore> findByGameKeyAndUserId(String gameKey,Long userId);List<GameScore> findTop10ByGameKeyAndUserIsNotNullOrderByScoreDescUpdatedAtAsc(String gameKey);}
