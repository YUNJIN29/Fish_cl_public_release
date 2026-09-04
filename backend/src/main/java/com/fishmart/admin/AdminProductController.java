package com.fishmart.admin;

import com.fishmart.admin.dto.UpsertProductRequest;
import com.fishmart.catalog.*;
import com.fishmart.catalog.dto.ProductDetailDto;
import com.fishmart.catalog.dto.ProductListItemDto;
import com.fishmart.common.ApiResponse;
import com.fishmart.common.BizException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
  private final ProductRepository productRepo;
  private final CategoryRepository categoryRepo;
  private final ProductImageRepository imageRepo;

  public AdminProductController(ProductRepository productRepo, CategoryRepository categoryRepo, ProductImageRepository imageRepo) {
    this.productRepo = productRepo;
    this.categoryRepo = categoryRepo;
    this.imageRepo = imageRepo;
  }

  @GetMapping
  public ApiResponse<Page<ProductListItemDto>> list(
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String tag,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "id"));
    return ApiResponse.ok(productRepo.search(categoryId, q, tag, null, pr).map(ProductListItemDto::from));
  }

  @GetMapping("/{id}")
  public ApiResponse<ProductDetailDto> detail(@PathVariable Long id) {
    Product p = productRepo.findById(id).orElseThrow(() -> new BizException("商品不存在"));
    List<String> images = imageRepo.findByProductIdOrderBySortOrderAscIdAsc(id).stream().map(ProductImage::getImageUrl).toList();
    return ApiResponse.ok(ProductDetailDto.from(p, images));
  }

  @PostMapping
  @Transactional
  public ApiResponse<Product> create(@Valid @RequestBody UpsertProductRequest req) {
    Category c = categoryRepo.findById(req.getCategoryId()).orElseThrow(() -> new BizException("分类不存在"));
    Product p = new Product();
    p.setCategory(c);
    p.setName(req.getName());
    p.setDescription(req.getDescription());
    p.setPriceCents(req.getPriceCents());
    p.setStock(req.getStock());
    p.setCoverImageUrl(req.getCoverImageUrl());
    p.setSymptomTags(req.getSymptomTags());
    p.setUsageAdvice(req.getUsageAdvice());
    p = productRepo.save(p);
    upsertImages(p, req.getImages());
    return ApiResponse.ok(p);
  }

  @PutMapping("/{id}")
  @Transactional
  public ApiResponse<Product> update(@PathVariable Long id, @Valid @RequestBody UpsertProductRequest req) {
    Product p = productRepo.findById(id).orElseThrow(() -> new BizException("商品不存在"));
    Category c = categoryRepo.findById(req.getCategoryId()).orElseThrow(() -> new BizException("分类不存在"));
    p.setCategory(c);
    p.setName(req.getName());
    p.setDescription(req.getDescription());
    p.setPriceCents(req.getPriceCents());
    p.setStock(req.getStock());
    p.setCoverImageUrl(req.getCoverImageUrl());
    p.setSymptomTags(req.getSymptomTags());
    p.setUsageAdvice(req.getUsageAdvice());
    p = productRepo.save(p);
    upsertImages(p, req.getImages());
    return ApiResponse.ok(p);
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<Product> status(@PathVariable Long id, @RequestParam ProductStatus status) {
    Product p = productRepo.findById(id).orElseThrow(() -> new BizException("商品不存在"));
    p.setStatus(status);
    return ApiResponse.ok(productRepo.save(p));
  }

  @DeleteMapping("/{id}")
  @Transactional
  public ApiResponse<Object> delete(@PathVariable Long id) {
    if (!productRepo.existsById(id)) throw new BizException("商品不存在");
    imageRepo.deleteByProductId(id);
    productRepo.deleteById(id);
    return ApiResponse.ok("DELETED", null);
  }

  private void upsertImages(Product p, List<String> images) {
    if (images == null) return;
    imageRepo.deleteByProductId(p.getId());
    int i = 0;
    for (String url : images) {
      if (url == null || url.isBlank()) continue;
      ProductImage img = new ProductImage();
      img.setProduct(p);
      img.setImageUrl(url);
      img.setSortOrder(i++);
      imageRepo.save(img);
    }
  }
}

