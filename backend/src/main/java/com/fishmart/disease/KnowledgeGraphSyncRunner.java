package com.fishmart.disease;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(21)
public class KnowledgeGraphSyncRunner implements CommandLineRunner {
  private final KnowledgeGraphService knowledgeGraphService;

  public KnowledgeGraphSyncRunner(KnowledgeGraphService knowledgeGraphService) {
    this.knowledgeGraphService = knowledgeGraphService;
  }

  @Override
  public void run(String... args) {
    knowledgeGraphService.rebuildFromDiseases();
  }
}
