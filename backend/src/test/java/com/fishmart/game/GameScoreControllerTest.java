package com.fishmart.game;

import com.fishmart.common.BizException;
import com.fishmart.security.AuthUser;
import com.fishmart.user.RoleName;
import com.fishmart.user.User;
import com.fishmart.user.UserRepository;
import com.fishmart.visit.IpLocationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameScoreControllerTest {
  @Mock GameScoreRepository scores;
  @Mock IpLocationService locations;
  @Mock UserRepository users;
  @Mock HttpServletRequest request;

  @Test void publicLeaderboardUsesAnonymousPlayerAliasWithoutNetworkData() {
    User user=new User();user.setId(7L);user.setUsername("private-name");
    GameScore score=new GameScore();score.setGameKey("snake");score.setUser(user);score.setIpAddress("203.0.113.8");score.setLocation("private-location");score.setScore(120);
    when(scores.findTop10ByGameKeyAndUserIsNotNullOrderByScoreDescUpdatedAtAsc("snake")).thenReturn(List.of(score));

    var item=new GameScoreController(scores,locations,users).list("snake",null).getData().get(0);

    assertThat(item.player()).startsWith("玩家-").doesNotContain("private-name","203.0.113.8","private-location");
    assertThat(item.score()).isEqualTo(120);
    assertThat(item.mine()).isFalse();
  }

  @Test void submissionBelongsToAuthenticatedAccount() {
    AuthUser auth=new AuthUser(7L,"owner",Set.of(RoleName.USER));
    User user=new User();user.setId(7L);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(scores.findByGameKeyAndUserId("snake",7L)).thenReturn(Optional.empty());
    when(users.findById(7L)).thenReturn(Optional.of(user));
    GameScore saved=new GameScore();saved.setGameKey("snake");saved.setUser(user);saved.setScore(120);
    when(scores.findTop10ByGameKeyAndUserIsNotNullOrderByScoreDescUpdatedAtAsc("snake")).thenReturn(List.of(saved));

    var result=new GameScoreController(scores,locations,users).submit(new GameScoreController.ScoreRequest("snake",120),request,auth);

    verify(scores).findByGameKeyAndUserId("snake",7L);
    verify(scores).save(any(GameScore.class));
    assertThat(result.getData().get(0).mine()).isTrue();
  }

  @Test void rejectsAnonymousOrImpossibleScores() {
    GameScoreController controller=new GameScoreController(scores,locations,users);
    assertThatThrownBy(()->controller.submit(new GameScoreController.ScoreRequest("snake",100),request,null)).isInstanceOf(BizException.class);
    AuthUser auth=new AuthUser(7L,"owner",Set.of(RoleName.USER));
    assertThatThrownBy(()->controller.submit(new GameScoreController.ScoreRequest("snake",15),request,auth)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("游戏规则");
    assertThatThrownBy(()->controller.submit(new GameScoreController.ScoreRequest("whack-a-mole",61),request,auth)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("游戏规则");
    verify(scores,never()).save(any());
  }
}
