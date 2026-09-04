package com.fishmart.rag;

import com.fishmart.common.ApiResponse;
import com.fishmart.rag.dto.RagDatasetDto;
import com.fishmart.rag.dto.RagDocumentDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rag")
public class RagController {
  private final RagDocumentRepository repository;
  private final RagSearchService searchService;

  public RagController(RagDocumentRepository repository, RagSearchService searchService) {
    this.repository = repository;
    this.searchService = searchService;
  }

  @GetMapping("/sources")
  public ApiResponse<List<RagDocumentDto>> sources() {
    return ApiResponse.ok(repository.findByEnabledTrueOrderByCreatedAtDesc().stream()
        .map(RagDocumentDto::from)
        .toList());
  }

  @GetMapping("/search")
  public ApiResponse<List<RagDocumentDto>> search(@RequestParam(defaultValue = "") String q,
                                                  @RequestParam(defaultValue = "8") int limit) {
    int safeLimit = Math.max(1, Math.min(limit, 20));
    return ApiResponse.ok(searchService.search(q, safeLimit).stream()
        .map(RagDocumentDto::from)
        .toList());
  }

  @GetMapping("/datasets")
  public ApiResponse<List<RagDatasetDto>> datasets() {
    return ApiResponse.ok(List.of(
        new RagDatasetDto(
            "WOAH Animal Diseases Portal",
            "World Organisation for Animal Health",
            "https://www.woah.org/en/what-we-do/animal-health-and-welfare/animal-diseases/",
            "权威疫病条目",
            "English",
            "构建水生动物法定疫病、宿主、病原、诊断章节索引。",
            "按 Aquatics 过滤后抓取疾病标题、宿主类群和 Manual/Code 链接，作为高置信来源元数据。"
        ),
        new RagDatasetDto(
            "WOAH Aquatic Animal Diseases Field Guide",
            "World Organisation for Animal Health",
            "https://www.woah.org/en/oie-work/aquatic-animals/",
            "现场识别资料",
            "English",
            "补充典型外观症状、采样提醒和现场初筛措辞。",
            "人工审核后提取 disease profile、gross signs、sampling checklist，避免把现场识别描述误写成确诊结论。"
        ),
        new RagDatasetDto(
            "WOAH Aquatic Manual / Aquatic Code",
            "World Organisation for Animal Health",
            "https://www.woah.org/en/what-we-do/standards/codes-and-manuals/",
            "诊断标准与国际规范",
            "English",
            "为报告补充确诊方法、采样、实验室检测和生物安全依据。",
            "按疾病章节切分，优先索引诊断方法、样品采集、易感宿主和风险管理段落。"
        ),
        new RagDatasetDto(
            "WAHIS World Animal Health Information System",
            "World Organisation for Animal Health",
            "https://wahis.woah.org/",
            "疫情事件与地理分布数据库",
            "English",
            "用于判断跨境疫病、地区流行趋势和预警信息。",
            "定期导出 aquatic listed diseases 事件，按病名、国家/地区、时间、宿主分块入库。"
        ),
        new RagDatasetDto(
            "Aquatic Animal Health Status Reviews",
            "USDA APHIS",
            "https://www.aphis.usda.gov/livestock-poultry-disease/aquaculture/health-status-reviews",
            "年度健康状态综述",
            "English",
            "补充重点水产病原监测、无疫状态和防控背景。",
            "把病原综述、监测依据和 WOAH 关联章节做成地区性背景资料，标注非中国本地适用限制。"
        )
    ));
  }
}
