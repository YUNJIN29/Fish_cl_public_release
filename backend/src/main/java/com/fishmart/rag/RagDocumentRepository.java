package com.fishmart.rag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RagDocumentRepository extends JpaRepository<RagDocument, Long> {
  List<RagDocument> findByEnabledTrueOrderByCreatedAtDesc();
}
