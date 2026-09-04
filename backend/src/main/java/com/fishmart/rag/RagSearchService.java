package com.fishmart.rag;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RagSearchService {
  private final RagDocumentRepository repository;

  public RagSearchService(RagDocumentRepository repository) {
    this.repository = repository;
  }

  public List<RagDocument> search(String query, int limit) {
    List<RagDocument> docs = repository.findByEnabledTrueOrderByCreatedAtDesc();
    if (query == null || query.isBlank()) {
      return docs.stream().limit(limit).toList();
    }

    List<String> terms = tokenize(query);
    if (terms.isEmpty()) {
      return docs.stream().limit(limit).toList();
    }

    return docs.stream()
        .map(doc -> new ScoredDocument(doc, score(doc, terms)))
        .filter(item -> item.score() > 0)
        .sorted(Comparator.comparingInt(ScoredDocument::score).reversed())
        .limit(limit)
        .map(ScoredDocument::document)
        .collect(Collectors.toList());
  }

  public String buildContext(List<RagDocument> docs) {
    return docs.stream()
        .map(doc -> String.format("【%s】来源：%s\n摘要：%s\n片段：%s\n链接：%s",
            doc.getTitle(),
            blankToDash(doc.getSourceName()),
            blankToDash(doc.getSummary()),
            truncate(doc.getContent(), 900),
            blankToDash(doc.getSourceUrl())))
        .collect(Collectors.joining("\n\n"));
  }

  private int score(RagDocument doc, List<String> terms) {
    String title = normalize(doc.getTitle());
    String tags = normalize(doc.getTags());
    String category = normalize(doc.getCategory());
    String summary = normalize(doc.getSummary());
    String content = normalize(doc.getContent());
    int score = 0;
    for (String term : terms) {
      String t = normalize(term);
      if (t.isBlank()) continue;
      if (title.contains(t)) score += 8;
      if (tags.contains(t)) score += 6;
      if (category.contains(t)) score += 4;
      if (summary.contains(t)) score += 3;
      if (content.contains(t)) score += 1;
    }
    if (terms.contains("草鱼出血病") && (title + tags + summary).contains("草鱼出血")) score += 28;
    if ((terms.contains("出血") || terms.contains("充血")) && (title + tags + summary).contains("草鱼出血")) score += 18;
    if ((terms.contains("白点") || terms.contains("小瓜虫")) && (title + tags + summary).contains("白点")) score += 24;
    if (terms.contains("蹭缸") && (title + tags + summary + content).contains("蹭缸")) score += 14;
    if ((terms.contains("白毛") || terms.contains("水霉")) && (title + tags + summary).contains("水霉")) score += 18;
    if ((terms.contains("浮头") || terms.contains("缺氧")) && (summary + content + tags).contains("水质")) score += 10;
    return score;
  }

  private List<String> tokenize(String query) {
    Set<String> terms = new LinkedHashSet<>();
    String normalized = query.replaceAll("[，。；、！？：,.!?;:\\[\\]【】()（）\\s]+", " ").trim();
    for (String part : normalized.split(" ")) {
      addTerm(terms, part);
    }

    List<String> known = List.of(
        "白点", "白毛", "水霉", "烂鳃", "烂鳍", "出血", "充血", "竖鳞", "腹水", "肠炎",
        "浮头", "缺氧", "鳃", "溃疡", "寄生虫", "病毒", "细菌", "真菌", "疫苗", "消毒",
        "草鱼", "锦鲤", "金鱼", "鲤鱼", "鲫鱼", "对虾", "鲑", "鲈", "罗非鱼",
        "氨氮", "亚硝酸盐", "pH", "水温", "死亡", "摄食", "蹭缸", "红斑", "腐烂",
        "腹部膨大", "眼球突出", "棉絮", "黏液", "打转", "离群", "换水", "高温", "低温"
    );
    known.stream().filter(query::contains).forEach(terms::add);
    expandSynonyms(terms);
    return new ArrayList<>(terms);
  }

  private void expandSynonyms(Set<String> terms) {
    if (terms.contains("出血") || terms.contains("充血") || terms.contains("红斑")) {
      terms.add("草鱼出血病");
      terms.add("败血");
      terms.add("赤皮");
    }
    if (terms.contains("白点") || terms.contains("蹭缸")) {
      terms.add("小瓜虫");
      terms.add("寄生虫");
    }
    if (terms.contains("白毛") || terms.contains("棉絮")) {
      terms.add("水霉");
      terms.add("真菌");
    }
    if (terms.contains("浮头") || terms.contains("缺氧")) {
      terms.add("烂鳃");
      terms.add("水质");
      terms.add("溶氧");
    }
    if (terms.contains("腹部膨大") || terms.contains("竖鳞") || terms.contains("眼球突出")) {
      terms.add("腹水");
      terms.add("竖鳞病");
      terms.add("细菌");
    }
    if (terms.contains("氨氮") || terms.contains("亚硝酸盐") || terms.contains("换水")) {
      terms.add("水质应激");
      terms.add("水质");
    }
  }

  private void addTerm(Set<String> terms, String term) {
    if (term == null) return;
    String value = term.trim();
    if (value.length() >= 2) terms.add(value);
  }

  private String normalize(String value) {
    return value == null ? "" : value.toLowerCase(Locale.ROOT);
  }

  private String blankToDash(String value) {
    return value == null || value.isBlank() ? "-" : value;
  }

  private String truncate(String value, int max) {
    if (value == null || value.isBlank()) return "-";
    String text = value.replaceAll("\\s+", " ").trim();
    return text.length() > max ? text.substring(0, max) + "..." : text;
  }

  private record ScoredDocument(RagDocument document, int score) {}
}
