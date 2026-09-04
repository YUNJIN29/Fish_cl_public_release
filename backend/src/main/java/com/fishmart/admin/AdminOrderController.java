package com.fishmart.admin;

import com.fishmart.common.ApiResponse;
import com.fishmart.order.Order;
import com.fishmart.order.OrderService;
import com.fishmart.order.OrderStatus;
import com.fishmart.order.dto.OrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
  private final OrderService orderService;

  public AdminOrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping
  public ApiResponse<Page<OrderDto>> list(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
    PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "id"));
    Page<Order> res = orderService.adminList(pr);
    return ApiResponse.ok(res.map(OrderDto::from));
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<OrderDto> status(@PathVariable Long id, @RequestParam OrderStatus status) {
    return ApiResponse.ok(OrderDto.from(orderService.adminUpdateStatus(id, status)));
  }
}

