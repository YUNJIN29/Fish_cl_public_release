package com.fishmart.disease;

import com.fishmart.common.ApiResponse;
import com.fishmart.common.BizException;
import com.fishmart.disease.dto.DiseaseDetailDto;
import com.fishmart.disease.dto.DiseaseListItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diseases")
public class DiseaseController {

  private final DiseaseRepository diseaseRepo;

  public DiseaseController(DiseaseRepository diseaseRepo) {
    this.diseaseRepo = diseaseRepo;
  }

  @GetMapping
  public ApiResponse<Page<DiseaseListItemDto>> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String type,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size
  ) {
    PageRequest pr = PageRequest.of(
        Math.max(page, 0),
        Math.min(Math.max(size, 1), 50),
        Sort.by(Sort.Direction.ASC, "id")
    );
    Page<DiseaseListItemDto> result = diseaseRepo.search(q, type, pr)
        .map(DiseaseListItemDto::from);
    return ApiResponse.ok(result);
  }

  @GetMapping("/{id}")
  public ApiResponse<DiseaseDetailDto> detail(@PathVariable Long id) {
    Disease d = diseaseRepo.findById(id)
        .orElseThrow(() -> new BizException("病害不存在"));
    return ApiResponse.ok(DiseaseDetailDto.from(d));
  }
}
