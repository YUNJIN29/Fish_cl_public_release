package com.fishmart.admin;

import com.fishmart.admin.dto.UpsertCategoryRequest;
import com.fishmart.catalog.Category;
import com.fishmart.catalog.CategoryRepository;
import com.fishmart.catalog.dto.CategoryDto;
import com.fishmart.common.ApiResponse;
import com.fishmart.common.BizException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {
  private final CategoryRepository repo;

  public AdminCategoryController(CategoryRepository repo) {
    this.repo = repo;
  }

  @GetMapping
  public ApiResponse<List<CategoryDto>> list() {
    List<CategoryDto> list = repo.findAll(Sort.by(Sort.Direction.DESC, "sortOrder").and(Sort.by("id")))
        .stream().map(CategoryDto::from).toList();
    return ApiResponse.ok(list);
  }

  @PostMapping
  public ApiResponse<CategoryDto> create(@Valid @RequestBody UpsertCategoryRequest req) {
    Category c = new Category();
    c.setName(req.getName());
    c.setSortOrder(req.getSortOrder());
    return ApiResponse.ok(CategoryDto.from(repo.save(c)));
  }

  @PutMapping("/{id}")
  public ApiResponse<CategoryDto> update(@PathVariable Long id, @Valid @RequestBody UpsertCategoryRequest req) {
    Category c = repo.findById(id).orElseThrow(() -> new BizException("分类不存在"));
    c.setName(req.getName());
    c.setSortOrder(req.getSortOrder());
    return ApiResponse.ok(CategoryDto.from(repo.save(c)));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Object> delete(@PathVariable Long id) {
    if (!repo.existsById(id)) throw new BizException("分类不存在");
    repo.deleteById(id);
    return ApiResponse.ok("DELETED", null);
  }
}

