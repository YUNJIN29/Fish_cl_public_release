package com.fishmart.game;

import com.fishmart.common.ApiResponse;
import com.fishmart.visit.IpLocationService;
import com.fishmart.security.AuthUser;
import com.fishmart.user.UserRepository;
import com.fishmart.common.BizException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/api/game-scores")
public class GameScoreController {
  private static final Set<String> GAMES=Set.of("1024","tetris","snake","whack-a-mole");
  private final GameScoreRepository repository;private final IpLocationService locations;private final UserRepository users;
  public GameScoreController(GameScoreRepository repository,IpLocationService locations,UserRepository users){this.repository=repository;this.locations=locations;this.users=users;}
  public record ScoreRequest(String game,long score){} public record RankItem(String player,long score,boolean mine){}
  @GetMapping("/{game}") @Transactional(readOnly=true) public ApiResponse<List<RankItem>> list(@PathVariable String game,@AuthenticationPrincipal AuthUser authUser){validate(game);Long currentId=authUser==null?null:authUser.getId();return ApiResponse.ok(repository.findTop10ByGameKeyAndUserIsNotNullOrderByScoreDescUpdatedAtAsc(game).stream().map(x->new RankItem(alias(x),x.getScore(),Objects.equals(currentId,x.getUser().getId()))).toList());}
  @PostMapping @Transactional public ApiResponse<List<RankItem>> submit(@RequestBody ScoreRequest req,HttpServletRequest request,@AuthenticationPrincipal AuthUser authUser){if(authUser==null)throw new BizException("未登录");validate(req.game());validateScore(req.game(),req.score());String ip=clientIp(request);GameScore item=repository.findByGameKeyAndUserId(req.game(),authUser.getId()).orElseGet(()->{GameScore x=new GameScore();x.setGameKey(req.game());x.setUser(users.findById(authUser.getId()).orElseThrow(()->new BizException("用户不存在")));x.setIpAddress(ip);x.setLocation(locations.resolve(ip));x.setScore(0);return x;});if(req.score()>item.getScore()){item.setScore(req.score());item.setIpAddress(ip);item.setUpdatedAt(Instant.now());repository.save(item);}return list(req.game(),authUser);}
  private void validate(String game){if(!GAMES.contains(game))throw new IllegalArgumentException("不支持的游戏");}
  private void validateScore(String game,long score){long max=switch(game){case "whack-a-mole"->60;case "snake"->4000;case "1024"->1000000;default->10000000;};if(score<0||score>max||(game.equals("snake")&&score%10!=0))throw new IllegalArgumentException("成绩不符合游戏规则");}
  private String clientIp(HttpServletRequest r){String x=r.getHeader("X-Forwarded-For");if(x!=null&&!x.isBlank())return x.split(",")[0].trim();x=r.getHeader("X-Real-IP");return x!=null&&!x.isBlank()?x.trim():r.getRemoteAddr();}
  private String alias(GameScore item){return "玩家-"+Integer.toUnsignedString(Objects.hash(item.getGameKey(),item.getUser().getId()),36).toUpperCase(Locale.ROOT);}
}
