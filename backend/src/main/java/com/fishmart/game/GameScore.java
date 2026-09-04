package com.fishmart.game;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter @Setter @Entity
@Table(name="game_scores", uniqueConstraints=@UniqueConstraint(name="uk_game_ip", columnNames={"game_key","ip_address"}))
public class GameScore {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="game_key",nullable=false,length=32) private String gameKey;
  @Column(name="ip_address",nullable=false,length=64) private String ipAddress;
  @Column(nullable=false) private long score;
  @Column(length=160) private String location;
  @Column(nullable=false) private Instant updatedAt=Instant.now();
}
