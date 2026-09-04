package com.fishmart.admin;

import com.fishmart.catalog.ProductRepository;
import com.fishmart.common.ApiResponse;
import com.fishmart.content.CommentRepository;
import com.fishmart.content.ContentStatus;
import com.fishmart.content.PostRepository;
import com.fishmart.order.OrderRepository;
import com.fishmart.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {
  private final UserRepository userRepo;
  private final ProductRepository productRepo;
  private final OrderRepository orderRepo;
  private final PostRepository postRepo;
  private final CommentRepository commentRepo;

  public AdminStatsController(UserRepository userRepo,
                              ProductRepository productRepo,
                              OrderRepository orderRepo,
                              PostRepository postRepo,
                              CommentRepository commentRepo) {
    this.userRepo = userRepo;
    this.productRepo = productRepo;
    this.orderRepo = orderRepo;
    this.postRepo = postRepo;
    this.commentRepo = commentRepo;
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> stats() {
    long users = userRepo.count();
    long products = productRepo.count();
    long orders = orderRepo.count();
    long pendingPosts = postRepo.search(null, ContentStatus.PENDING, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
    long pendingComments = commentRepo.findByStatusOrderByIdDesc(ContentStatus.PENDING, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
    long pendingContent = pendingPosts + pendingComments;
    return ApiResponse.ok(Map.of(
        "users", users,
        "products", products,
        "orders", orders,
        "pendingContent", pendingContent
    ));
  }

  @GetMapping("/trend")
  public ApiResponse<List<Map<String, Object>>> trend() {
    ZoneId zone = ZoneId.systemDefault();
    LocalDate today = LocalDate.now(zone);
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");

    Map<LocalDate, Long> orderDaily = new LinkedHashMap<>();
    Map<LocalDate, Long> userDaily = new LinkedHashMap<>();
    for (int i = 6; i >= 0; i--) {
      LocalDate d = today.minusDays(i);
      orderDaily.put(d, 0L);
      userDaily.put(d, 0L);
    }

    orderRepo.findAll().forEach(o -> {
      LocalDate d = o.getCreatedAt().atZone(zone).toLocalDate();
      if (orderDaily.containsKey(d)) orderDaily.put(d, orderDaily.get(d) + 1);
    });
    userRepo.findAll().forEach(u -> {
      LocalDate d = u.getCreatedAt().atZone(zone).toLocalDate();
      if (userDaily.containsKey(d)) userDaily.put(d, userDaily.get(d) + 1);
    });

    List<Map<String, Object>> result = orderDaily.keySet().stream()
        .map(d -> Map.<String, Object>of(
            "date", d.format(fmt),
            "orders", orderDaily.get(d),
            "users", userDaily.get(d)
        ))
        .toList();
    return ApiResponse.ok(result);
  }
}

