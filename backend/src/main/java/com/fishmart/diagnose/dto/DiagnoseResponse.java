package com.fishmart.diagnose.dto;

import com.fishmart.catalog.dto.ProductListItemDto;
import com.fishmart.disease.dto.DiseaseListItemDto;
import com.fishmart.rag.dto.RagDocumentDto;
import lombok.Data;

import java.util.List;

@Data
public class DiagnoseResponse {
  private String report;
  private boolean saved;
  private String imageUrl;
  private List<DiseaseListItemDto> relatedDiseases;
  private List<ProductListItemDto> relatedProducts;
  private List<RagDocumentDto> ragReferences;
  private String confidence;
  private List<String> evidence;
  private List<String> sources;
  private List<String> warnings;
  private List<String> nextSteps;

  public DiagnoseResponse(String report,
                          boolean saved,
                          String imageUrl,
                          List<DiseaseListItemDto> relatedDiseases,
                          List<ProductListItemDto> relatedProducts,
                          List<RagDocumentDto> ragReferences) {
    this.report = report;
    this.saved = saved;
    this.imageUrl = imageUrl;
    this.relatedDiseases = relatedDiseases;
    this.relatedProducts = relatedProducts;
    this.ragReferences = ragReferences;
    this.confidence = relatedDiseases.isEmpty() ? "LOW" : "MEDIUM";
    this.evidence = relatedDiseases.stream().limit(3).map(item -> "知识库候选：" + item.getName()).toList();
    this.sources = ragReferences.stream().map(RagDocumentDto::getTitle).distinct().toList();
    this.warnings = List.of("结果仅用于学习和辅助判断，不能替代现场专业诊断", "出现大量死亡、浮头或急性水质异常时请立即联系水产技术人员");
    this.nextSteps = List.of("复测水温、pH、氨氮与亚硝酸盐", "隔离病鱼并记录 24 小时变化", "用药前核对标签、当地规范和休药期");
  }
}
