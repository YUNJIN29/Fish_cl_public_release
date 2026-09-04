package com.fishmart.catalog.dto;

import com.fishmart.catalog.Product;
import com.fishmart.catalog.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductListItemDto {
  private Long id;
  private Long categoryId;
  private String categoryName;
  private String name;
  private int priceCents;
  private int stock;
  private String coverImageUrl;
  private ProductStatus status;

  public static ProductListItemDto from(Product p) {
    return new ProductListItemDto(
        p.getId(),
        p.getCategory().getId(),
        p.getCategory().getName(),
        p.getName(),
        p.getPriceCents(),
        p.getStock(),
        p.getCoverImageUrl(),
        p.getStatus()
    );
  }
}

