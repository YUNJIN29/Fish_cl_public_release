package com.fishmart.catalog.dto;

import com.fishmart.catalog.Category;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryDto {
  private Long id;
  private String name;
  private int sortOrder;

  public static CategoryDto from(Category c) {
    return new CategoryDto(c.getId(), c.getName(), c.getSortOrder());
  }
}

