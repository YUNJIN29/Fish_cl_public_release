package com.fishmart.disease;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "knowledge_graph_nodes",
    indexes = {
        @Index(name = "idx_kg_node_graph_id", columnList = "graphNodeId", unique = true),
        @Index(name = "idx_kg_node_category", columnList = "categoryIndex")
    }
)
public class KnowledgeGraphNode {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 180)
  private String graphNodeId;

  @Column(nullable = false, length = 180)
  private String name;

  @Column(nullable = false)
  private int categoryIndex;

  @Column(length = 64)
  private String value;

  @Column(nullable = false)
  private int symbolSize;

  @Column(length = 64)
  private String diseaseType;

  @Lob
  private String description;

  @Lob
  private String symptomsSummary;

  @Lob
  private String treatment;

  @Lob
  private String prevention;

  @Column(length = 64)
  private String prevalentTemp;

  @Column(length = 256)
  private String detailUrl;
}
