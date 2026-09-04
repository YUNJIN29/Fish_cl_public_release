package com.fishmart.disease;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "knowledge_graph_categories")
public class KnowledgeGraphCategory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String name;

  @Column(nullable = false)
  private int sortOrder;
}
