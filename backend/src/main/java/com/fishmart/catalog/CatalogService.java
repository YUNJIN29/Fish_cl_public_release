package com.fishmart.catalog;

import com.fishmart.catalog.dto.ProductDetailDto;
import com.fishmart.catalog.dto.ProductListItemDto;
import com.fishmart.common.BizException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {
  private final ProductRepository productRepo;
  private final ProductImageRepository imageRepo;

  public CatalogService(ProductRepository productRepo, ProductImageRepository imageRepo) {
    this.productRepo = productRepo;
    this.imageRepo = imageRepo;
  }

  public Page<ProductListItemDto> listProducts(Long categoryId, String q, String tag, Pageable pageable) {
    return productRepo.search(categoryId, q, tag, ProductStatus.ON, pageable).map(ProductListItemDto::from);
  }

  public ProductDetailDto getProduct(Long id) {
    Product p = productRepo.findById(id).orElseThrow(() -> new BizException("商品不存在"));
    if (p.getStatus() != ProductStatus.ON) {
      throw new BizException("商品已下架");
    }
    List<String> images = imageRepo.findByProductIdOrderBySortOrderAscIdAsc(id).stream().map(ProductImage::getImageUrl).toList();
    return ProductDetailDto.from(p, images);
  }
}

