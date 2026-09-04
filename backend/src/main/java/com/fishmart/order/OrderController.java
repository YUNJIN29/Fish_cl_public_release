package com.fishmart.order;

import com.fishmart.common.ApiResponse;
import com.fishmart.order.dto.CreateOrderRequest;
import com.fishmart.order.dto.OrderDto;
import com.fishmart.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public ApiResponse<OrderDto> create(Authentication authentication, @Valid @RequestBody CreateOrderRequest req) {
    AuthUser u = (AuthUser) authentication.getPrincipal();
    return ApiResponse.ok(OrderDto.from(orderService.create(u.getId(), req)));
  }

  @GetMapping
  public ApiResponse<Page<OrderDto>> myOrders(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    AuthUser u = (AuthUser) authentication.getPrincipal();
    PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "id"));
    return ApiResponse.ok(orderService.myOrders(u.getId(), pr).map(OrderDto::from));
  }

  @GetMapping("/{id}")
  public ApiResponse<OrderDto> myOrder(Authentication authentication, @PathVariable Long id) {
    AuthUser u = (AuthUser) authentication.getPrincipal();
    return ApiResponse.ok(OrderDto.from(orderService.getMyOrder(u.getId(), id)));
  }
}

