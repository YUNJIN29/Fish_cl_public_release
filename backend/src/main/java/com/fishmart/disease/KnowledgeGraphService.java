package com.fishmart.disease;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class KnowledgeGraphService {
  private static final List<String> CATEGORIES = List.of(
      "病害", "病害类型", "症状", "病原/诱因", "治疗", "预防", "鱼种", "流行地区", "温度"
  );
  private static final Pattern SPLIT_PATTERN = Pattern.compile("[,，、；;。]");

  private final DiseaseRepository diseaseRepo;
  private final KnowledgeGraphCategoryRepository categoryRepo;
  private final KnowledgeGraphNodeRepository nodeRepo;
  private final KnowledgeGraphLinkRepository linkRepo;

  public KnowledgeGraphService(
      DiseaseRepository diseaseRepo,
      KnowledgeGraphCategoryRepository categoryRepo,
      KnowledgeGraphNodeRepository nodeRepo,
      KnowledgeGraphLinkRepository linkRepo
  ) {
    this.diseaseRepo = diseaseRepo;
    this.categoryRepo = categoryRepo;
    this.nodeRepo = nodeRepo;
    this.linkRepo = linkRepo;
  }

  @Transactional(readOnly = true)
  public KnowledgeGraphController.GraphData graphData() {
    List<KnowledgeGraphCategory> categoryEntities = categoryRepo.findAll(Sort.by("sortOrder").ascending());
    List<KnowledgeGraphNode> nodeEntities = nodeRepo.findAll(Sort.by("id").ascending());
    List<KnowledgeGraphLink> linkEntities = linkRepo.findAll(Sort.by("id").ascending());

    List<KnowledgeGraphController.Category> categories = categoryEntities.stream()
        .map(item -> new KnowledgeGraphController.Category(item.getName()))
        .toList();
    List<KnowledgeGraphController.Node> nodes = nodeEntities.stream()
        .map(item -> new KnowledgeGraphController.Node(
            item.getGraphNodeId(),
            item.getName(),
            item.getCategoryIndex(),
            item.getValue(),
            item.getSymbolSize(),
            item.getDiseaseType(),
            item.getDescription(),
            item.getSymptomsSummary(),
            item.getTreatment(),
            item.getPrevention(),
            item.getPrevalentTemp(),
            item.getDetailUrl()
        ))
        .toList();
    List<KnowledgeGraphController.Link> links = linkEntities.stream()
        .map(item -> new KnowledgeGraphController.Link(
            item.getSourceNodeId(),
            item.getTargetNodeId(),
            item.getName(),
            item.getValue(),
            item.getRelationType()
        ))
        .toList();

    return new KnowledgeGraphController.GraphData(
        categories,
        nodes,
        links,
        new KnowledgeGraphController.GraphStats(
            (int) nodeRepo.countByValue("病害"),
            nodes.size(),
            links.size(),
            categories.size(),
            (int) linkRepo.countByRelationType("病害关联")
        )
    );
  }

  @Transactional
  public void rebuildFromDiseases() {
    GraphBuilder builder = new GraphBuilder();
    List<Disease> diseases = diseaseRepo.findAll(Sort.by("id").ascending());
    diseases.forEach(builder::addDisease);
    builder.addRelatedDiseaseLinks(diseases);
    PersistableGraph graph = builder.buildPersistable();

    linkRepo.deleteAllInBatch();
    nodeRepo.deleteAllInBatch();
    categoryRepo.deleteAllInBatch();

    categoryRepo.saveAll(graph.categories());
    nodeRepo.saveAll(graph.nodes());
    linkRepo.saveAll(graph.links());
  }

  private record PersistableGraph(
      List<KnowledgeGraphCategory> categories,
      List<KnowledgeGraphNode> nodes,
      List<KnowledgeGraphLink> links
  ) {}

  private static class GraphBuilder {
    private final Map<String, KnowledgeGraphNode> nodes = new LinkedHashMap<>();
    private final Map<String, KnowledgeGraphLink> links = new LinkedHashMap<>();

    void addDisease(Disease disease) {
      String diseaseId = nodeKey("病害", disease.getName());
      KnowledgeGraphNode diseaseNode = new KnowledgeGraphNode();
      diseaseNode.setGraphNodeId(diseaseId);
      diseaseNode.setName(disease.getName());
      diseaseNode.setCategoryIndex(categoryIndex("病害"));
      diseaseNode.setValue("病害");
      diseaseNode.setSymbolSize(34);
      diseaseNode.setDiseaseType(disease.getDiseaseType());
      diseaseNode.setDescription(disease.getDescription());
      diseaseNode.setSymptomsSummary(disease.getSymptomsSummary());
      diseaseNode.setTreatment(disease.getTreatment());
      diseaseNode.setPrevention(disease.getPrevention());
      diseaseNode.setPrevalentTemp(disease.getPrevalentTemp());
      diseaseNode.setDetailUrl("/fish/diseases/" + disease.getId());
      nodes.put(diseaseId, diseaseNode);

      addSingle(diseaseId, "病害类型", disease.getDiseaseType(), "属于");
      addList(diseaseId, "鱼种", disease.getAffectedSpecies(), "危害");
      addList(diseaseId, "流行地区", disease.getPrevalentRegions(), "流行于");
      addSingle(diseaseId, "温度", disease.getPrevalentTemp(), "高发温度");
      addPhrases(diseaseId, "症状", disease.getSymptomsSummary(), "表现为", 7);
      addPhrases(diseaseId, "病原/诱因", disease.getCauses(), "由/因", 6);
      addPhrases(diseaseId, "治疗", disease.getTreatment(), "处理", 6);
      addPhrases(diseaseId, "预防", disease.getPrevention(), "预防", 5);
    }

    void addRelatedDiseaseLinks(List<Disease> diseases) {
      for (int i = 0; i < diseases.size(); i++) {
        Disease left = diseases.get(i);
        String leftId = nodeKey("病害", left.getName());
        int addedForDisease = 0;
        for (int j = i + 1; j < diseases.size(); j++) {
          Disease right = diseases.get(j);
          String relation = relatedRelation(left, right);
          if (relation == null) continue;
          String rightId = nodeKey("病害", right.getName());
          addDiseaseRelation(leftId, rightId, relation);
          addedForDisease++;
          if (addedForDisease >= 5) break;
        }
      }
    }

    private void addSingle(String source, String category, String text, String relation) {
      if (isBlank(text)) return;
      addNodeAndLink(source, category, clean(text), relation);
    }

    private void addList(String source, String category, String text, String relation) {
      split(text).forEach(item -> addNodeAndLink(source, category, item, relation));
    }

    private void addPhrases(String source, String category, String text, String relation, int limit) {
      split(text).stream()
          .map(GraphBuilder::trimPhrase)
          .filter(item -> item.length() >= 2)
          .limit(limit)
          .forEach(item -> addNodeAndLink(source, category, item, relation));
    }

    private void addNodeAndLink(String source, String category, String name, String relation) {
      String target = nodeKey(category, name);
      nodes.computeIfAbsent(target, key -> {
        KnowledgeGraphNode node = new KnowledgeGraphNode();
        node.setGraphNodeId(key);
        node.setName(name);
        node.setCategoryIndex(categoryIndex(category));
        node.setValue(category);
        node.setSymbolSize(symbolSize(category));
        return node;
      });
      addLink(source, target, relation, relation, "属性关系");
    }

    private void addDiseaseRelation(String source, String target, String relation) {
      addLink(source, target, relation, relation, "病害关联");
    }

    private void addLink(String source, String target, String name, String value, String relationType) {
      String linkId = source + "->" + target + ":" + name;
      links.computeIfAbsent(linkId, key -> {
        KnowledgeGraphLink link = new KnowledgeGraphLink();
        link.setSourceNodeId(source);
        link.setTargetNodeId(target);
        link.setUniqueKey(key);
        link.setName(name);
        link.setValue(value);
        link.setRelationType(relationType);
        return link;
      });
    }

    PersistableGraph buildPersistable() {
      List<KnowledgeGraphCategory> categories = new ArrayList<>();
      for (int i = 0; i < CATEGORIES.size(); i++) {
        KnowledgeGraphCategory category = new KnowledgeGraphCategory();
        category.setName(CATEGORIES.get(i));
        category.setSortOrder(i);
        categories.add(category);
      }
      return new PersistableGraph(categories, new ArrayList<>(nodes.values()), new ArrayList<>(links.values()));
    }

    private static String relatedRelation(Disease left, Disease right) {
      if (!isBlank(left.getDiseaseType()) && left.getDiseaseType().equals(right.getDiseaseType())) {
        return "同类病害";
      }
      Set<String> sharedSpecies = intersection(split(left.getAffectedSpecies()), split(right.getAffectedSpecies()));
      if (!sharedSpecies.isEmpty()) {
        return "共同鱼种：" + sharedSpecies.iterator().next();
      }
      Set<String> sharedSymptoms = intersection(keywordSet(left.getSymptomsSummary()), keywordSet(right.getSymptomsSummary()));
      if (!sharedSymptoms.isEmpty()) {
        return "相似症状：" + sharedSymptoms.iterator().next();
      }
      return null;
    }

    private static Set<String> intersection(List<String> left, List<String> right) {
      Set<String> result = new LinkedHashSet<>(left);
      result.retainAll(new LinkedHashSet<>(right));
      return result;
    }

    private static List<String> keywordSet(String text) {
      if (isBlank(text)) return List.of();
      List<String> known = List.of(
          "出血", "充血", "红斑", "溃疡", "腹水", "浮头", "烂鳃", "烂鳍", "白点", "白斑",
          "白毛", "体色发黑", "食欲减退", "死亡", "眼球突出", "黏液", "打转", "消瘦"
      );
      return known.stream().filter(text::contains).toList();
    }

    private static List<String> split(String text) {
      if (isBlank(text)) return List.of();
      List<String> values = new ArrayList<>();
      for (String part : SPLIT_PATTERN.split(text)) {
        String cleaned = clean(part);
        if (!cleaned.isBlank()) values.add(cleaned);
      }
      return values;
    }

    private static String trimPhrase(String value) {
      String text = clean(value);
      return text.length() > 30 ? text.substring(0, 30) : text;
    }

    private static String clean(String value) {
      return value == null ? "" : value.replaceAll("\\s+", "").trim();
    }

    private static boolean isBlank(String value) {
      return value == null || value.isBlank();
    }

    private static String nodeKey(String category, String name) {
      return category + ":" + clean(name);
    }

    private static int categoryIndex(String category) {
      return Math.max(CATEGORIES.indexOf(category), 0);
    }

    private static int symbolSize(String category) {
      return switch (category) {
        case "病害类型" -> 24;
        case "症状" -> 18;
        case "病原/诱因" -> 17;
        case "治疗" -> 16;
        case "预防" -> 15;
        case "鱼种" -> 18;
        case "流行地区", "温度" -> 14;
        default -> 12;
      };
    }
  }
}
