package com.fishmart.personal;

import com.fishmart.common.ApiResponse;
import com.fishmart.content.FavoriteRepository;
import com.fishmart.content.PostRepository;
import com.fishmart.content.dto.PostDto;
import com.fishmart.diagnose.DiagnoseRecordRepository;
import com.fishmart.diagnose.dto.DiagnoseRecordDto;
import com.fishmart.order.OrderRepository;
import com.fishmart.order.dto.OrderDto;
import com.fishmart.personal.dto.PersonalOverviewDto;
import com.fishmart.security.AuthUser;
import com.fishmart.user.User;
import com.fishmart.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

@RestController
@RequestMapping("/api/personal")
public class PersonalController {
  private final UserRepository userRepo;
  private final DiagnoseRecordRepository diagnoseRepo;
  private final OrderRepository orderRepo;
  private final PostRepository postRepo;
  private final FavoriteRepository favoriteRepo;

  public PersonalController(UserRepository userRepo,
                            DiagnoseRecordRepository diagnoseRepo,
                            OrderRepository orderRepo,
                            PostRepository postRepo,
                            FavoriteRepository favoriteRepo) {
    this.userRepo = userRepo;
    this.diagnoseRepo = diagnoseRepo;
    this.orderRepo = orderRepo;
    this.postRepo = postRepo;
    this.favoriteRepo = favoriteRepo;
  }

  @GetMapping("/overview")
  public ApiResponse<PersonalOverviewDto> overview(@AuthenticationPrincipal AuthUser authUser) {
    User user = userRepo.findById(authUser.getId()).orElseThrow();
    PageRequest recentByTime = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"));
    PageRequest recentOrders = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "id"));

    PersonalOverviewDto.Stats stats = new PersonalOverviewDto.Stats(
        diagnoseRepo.countByUserId(user.getId()),
        orderRepo.countByUserId(user.getId()),
        postRepo.countByUserId(user.getId()),
        favoriteRepo.countByUserId(user.getId())
    );

    return ApiResponse.ok(new PersonalOverviewDto(
        profileCompletion(user),
        stats,
        diagnoseRepo.findByUserIdOrderByCreatedAtDesc(user.getId(), recentByTime).map(DiagnoseRecordDto::from).toList(),
        orderRepo.findByUserIdOrderByIdDesc(user.getId(), recentOrders).map(OrderDto::from).toList(),
        postRepo.findByUserIdOrderByCreatedAtDesc(user.getId(), recentByTime).map(post -> PostDto.from(post, false)).toList()
    ));
  }

  private int profileCompletion(User user) {
    long filled = Stream.of(
        user.getNickname(),
        user.getAvatarUrl(),
        user.getEmail(),
        user.getPhone(),
        user.getFarmName(),
        user.getFarmLocation(),
        user.getMainSpecies(),
        user.getBio()
    ).filter(value -> value != null && !value.isBlank()).count();
    return (int) Math.round(filled * 100.0 / 8.0);
  }
}
