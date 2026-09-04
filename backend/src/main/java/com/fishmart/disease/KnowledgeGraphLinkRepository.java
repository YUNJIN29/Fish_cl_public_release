package com.fishmart.disease;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeGraphLinkRepository extends JpaRepository<KnowledgeGraphLink, Long> {
  long countByRelationType(String relationType);
}
