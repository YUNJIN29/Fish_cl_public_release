package com.fishmart.disease;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeGraphNodeRepository extends JpaRepository<KnowledgeGraphNode, Long> {
  long countByValue(String value);
}
