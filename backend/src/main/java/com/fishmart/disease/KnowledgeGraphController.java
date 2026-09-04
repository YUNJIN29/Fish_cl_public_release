package com.fishmart.disease;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-graph")
public class KnowledgeGraphController {
  private final KnowledgeGraphService knowledgeGraphService;

  public KnowledgeGraphController(KnowledgeGraphService knowledgeGraphService) {
    this.knowledgeGraphService = knowledgeGraphService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public GraphData graphData() {
    return knowledgeGraphService.graphData();
  }

  public record GraphData(List<Category> categories, List<Node> nodes, List<Link> links, GraphStats stats) {}
  public record Category(String name) {}
  public record Link(String source, String target, String name, String value, String relationType) {}
  public record GraphStats(int diseaseCount, int nodeCount, int linkCount, int categoryCount, int relatedDiseaseLinkCount) {}

  public record Node(
      String id,
      String name,
      int category,
      String value,
      int symbolSize,
      String diseaseType,
      String description,
      String symptomsSummary,
      String treatment,
      String prevention,
      String prevalentTemp,
      String detailUrl
  ) {}
}
