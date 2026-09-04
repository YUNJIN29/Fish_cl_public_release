package com.fishmart.diagnose.dto;

import com.fishmart.catalog.dto.ProductListItemDto;
import com.fishmart.disease.dto.DiseaseListItemDto;
import com.fishmart.rag.dto.RagDocumentDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DiagnoseResponse {
  private String report;
  private boolean saved;
  private String imageUrl;
  private List<DiseaseListItemDto> relatedDiseases;
  private List<ProductListItemDto> relatedProducts;
  private List<RagDocumentDto> ragReferences;
}
