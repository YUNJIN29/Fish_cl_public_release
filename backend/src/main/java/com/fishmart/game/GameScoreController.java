package com.fishmart.game;

import com.fishmart.common.ApiResponse;
import com.fishmart.visit.IpLocationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/api/game-scores")
public class GameScoreController {
  private static final Set<String> GAMES=Set.of("1024","tetris","snake","whack-a-mole");
  private final GameScoreRepository repository;private final IpLocationService locations;
  public GameScoreController(GameScoreRepository repository,IpLocationService locations){this.repository=repository;this.locations=locations;}
  public record ScoreRequest(String game,long score){} public record RankItem(String ip,String location,long score){}
  @GetMapping("/{game}") public ApiResponse<List<RankItem>> list(@PathVariable String game){validate(game);return ApiResponse.ok(repository.findTop10ByGameKeyOrderByScoreDescUpdatedAtAsc(game).stream().map(x->new RankItem(mask(x.getIpAddress()),x.getLocation(),x.getScore())).toList());}
  @PostMapping @Transactional public ApiResponse<List<RankItem>> submit(@RequestBody ScoreRequest req,HttpServletRequest request){validate(req.game());if(req.score()<0||req.score()>100000000)throw new IllegalArgumentException("成绩无效");String ip=clientIp(request);GameScore item=repository.findByGameKeyAndIpAddress(req.game(),ip).orElseGet(()->{GameScore x=new GameScore();x.setGameKey(req.game());x.setIpAddress(ip);x.setLocation(locations.resolve(ip));x.setScore(0);return x;});if(req.score()>item.getScore()){item.setScore(req.score());item.setUpdatedAt(Instant.now());repository.save(item);}return list(req.game());}
  private void validate(String game){if(!GAMES.contains(game))throw new IllegalArgumentException("不支持的游戏");}
  private String clientIp(HttpServletRequest r){String x=r.getHeader("X-Forwarded-For");if(x!=null&&!x.isBlank())return x.split(",")[0].trim();x=r.getHeader("X-Real-IP");return x!=null&&!x.isBlank()?x.trim():r.getRemoteAddr();}
  private String mask(String ip){if(ip.contains(".")){String[] p=ip.split("\\.");return p.length==4?p[0]+"."+p[1]+".*.*":"*.*";}String[] p=ip.split(":",-1);if(p.length>2){p[p.length-1]="*";p[p.length-2]="*";return String.join(":",p);}return "*:*";}
}
