package com.fishmart.catalog.dto;

import com.fishmart.catalog.Product;
import com.fishmart.catalog.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductDetailDto {
  private Long id;
  private Long categoryId;
  private String categoryName;
  private String name;
  private String description;
  private int priceCents;
  private int stock;
  private String coverImageUrl;
  private String symptomTags;
  private String usageAdvice;
  private ProductStatus status;
  private List<String> images;

  public static ProductDetailDto from(Product p, List<String> images) {
    return new ProductDetailDto(
        p.getId(),
        p.getCategory().getId(),
        p.getCategory().getName(),
        p.getName(),
        p.getDescription(),
        p.getPriceCents(),
        p.getStock(),
        p.getCoverImageUrl(),
        p.getSymptomTags(),
        p.getUsageAdvice(),
        p.getStatus(),
        images
    );
  }
}

