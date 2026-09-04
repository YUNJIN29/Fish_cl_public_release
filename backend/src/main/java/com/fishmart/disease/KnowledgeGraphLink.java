package com.fishmart.disease;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "knowledge_graph_links",
    indexes = {
        @Index(name = "idx_kg_link_source", columnList = "sourceNodeId"),
        @Index(name = "idx_kg_link_target", columnList = "targetNodeId"),
        @Index(name = "idx_kg_link_unique_key", columnList = "uniqueKey", unique = true)
    }
)
public class KnowledgeGraphLink {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 180)
  private String sourceNodeId;

  @Column(nullable = false, length = 180)
  private String targetNodeId;

  @Column(nullable = false, length = 180)
  private String uniqueKey;

  @Column(length = 128)
  private String name;

  @Column(length = 128)
  private String value;

  @Column(length = 64)
  private String relationType;
}
