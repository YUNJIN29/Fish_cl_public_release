package com.fishmart.order.dto;

import com.fishmart.order.Order;
import com.fishmart.order.OrderItem;
import com.fishmart.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderDto {
  private Long id;
  private String orderNo;
  private int totalCents;
  private OrderStatus status;
  private Instant createdAt;
  private List<Item> items;

  @Data
  @AllArgsConstructor
  public static class Item {
    private Long productId;
    private String productName;
    private String coverImageUrl;
    private int quantity;
    private int unitPriceCents;
  }

  public static OrderDto from(Order o) {
    List<Item> items = o.getItems().stream().map(OrderDto::fromItem).toList();
    return new OrderDto(o.getId(), o.getOrderNo(), o.getTotalCents(), o.getStatus(), o.getCreatedAt(), items);
  }

  private static Item fromItem(OrderItem it) {
    return new Item(
        it.getProduct().getId(),
        it.getProduct().getName(),
        it.getProduct().getCoverImageUrl(),
        it.getQuantity(),
        it.getUnitPriceCents()
    );
  }
}

