package com.fishmart.game;

import com.fishmart.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter @Setter @Entity
@Table(name="game_scores", uniqueConstraints=@UniqueConstraint(name="uk_game_user", columnNames={"game_key","user_id"}))
public class GameScore {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="game_key",nullable=false,length=32) private String gameKey;
  @Column(name="ip_address",nullable=false,length=64) private String ipAddress;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id") private User user;
  @Column(nullable=false) private long score;
  @Column(length=160) private String location;
  @Column(nullable=false) private Instant updatedAt=Instant.now();
}
