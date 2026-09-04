package com.fishmart.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
  @NotEmpty
  @Valid
  private List<Item> items;

  @Data
  public static class Item {
    @NotNull
    private Long productId;
    @NotNull
    @Min(1)
    private Integer quantity;
  }
}

