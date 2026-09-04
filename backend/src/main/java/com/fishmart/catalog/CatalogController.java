package com.fishmart.catalog;

import com.fishmart.catalog.dto.CategoryDto;
import com.fishmart.catalog.dto.ProductDetailDto;
import com.fishmart.catalog.dto.ProductListItemDto;
import com.fishmart.common.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {
  private final CategoryRepository categoryRepo;
  private final CatalogService catalogService;

  public CatalogController(CategoryRepository categoryRepo, CatalogService catalogService) {
    this.categoryRepo = categoryRepo;
    this.catalogService = catalogService;
  }

  @GetMapping("/categories")
  public ApiResponse<List<CategoryDto>> categories() {
    List<CategoryDto> list = categoryRepo.findAll(Sort.by(Sort.Direction.DESC, "sortOrder").and(Sort.by("id")))
        .stream().map(CategoryDto::from).toList();
    return ApiResponse.ok(list);
  }

  @GetMapping("/products")
  public ApiResponse<Page<ProductListItemDto>> products(
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String tag,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "id"));
    return ApiResponse.ok(catalogService.listProducts(categoryId, q, tag, pr));
  }

  @GetMapping("/products/{id}")
  public ApiResponse<ProductDetailDto> product(@PathVariable Long id) {
    return ApiResponse.ok(catalogService.getProduct(id));
  }
}

