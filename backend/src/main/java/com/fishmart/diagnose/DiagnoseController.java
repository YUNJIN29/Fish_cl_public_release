package com.fishmart.diagnose;

import com.fishmart.catalog.Product;
import com.fishmart.catalog.ProductRepository;
import com.fishmart.catalog.ProductStatus;
import com.fishmart.catalog.dto.ProductListItemDto;
import com.fishmart.common.ApiResponse;
import com.fishmart.config.AppProperties;
import com.fishmart.disease.Disease;
import com.fishmart.disease.DiseaseRepository;
import com.fishmart.disease.dto.DiseaseListItemDto;
import com.fishmart.diagnose.dto.DiagnoseRecordDto;
import com.fishmart.diagnose.dto.DiagnoseResponse;
import com.fishmart.rag.RagDocument;
import com.fishmart.rag.RagSearchService;
import com.fishmart.rag.dto.RagDocumentDto;
import com.fishmart.security.AuthUser;
import com.fishmart.user.User;
import com.fishmart.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/diagnose")
public class DiagnoseController {

  private final AppProperties props;
  private final DiseaseRepository diseaseRepo;
  private final ProductRepository productRepo;
  private final DiagnoseRecordRepository recordRepo;
  private final UserRepository userRepo;
  private final RagSearchService ragSearchService;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  public DiagnoseController(AppProperties props, 
                            DiseaseRepository diseaseRepo, 
                            ProductRepository productRepo,
                            DiagnoseRecordRepository recordRepo,
                            UserRepository userRepo,
                            RagSearchService ragSearchService,
                            ObjectMapper objectMapper) {
    this.props = props;
    this.diseaseRepo = diseaseRepo;
    this.productRepo = productRepo;
    this.recordRepo = recordRepo;
    this.userRepo = userRepo;
    this.ragSearchService = ragSearchService;
    this.objectMapper = objectMapper;
    
    // 配置 RestTemplate 超时时间
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(5000);
    factory.setReadTimeout(60000); // 60秒读超时
    this.restTemplate = new RestTemplate(factory);
  }

  @PostMapping
  public ApiResponse<DiagnoseResponse> diagnose(@Valid @RequestBody DiagnoseRequest req,
                                                @AuthenticationPrincipal AuthUser authUser) {
    String baseUrl = props.getAi().getGlm4BaseUrl();
    String apiKey  = props.getAi().getApiKey();

    // 1. RAG 检索：从用户描述、鱼种、水质和环境信息中提取关键词，召回结构化病害与资料片段。
    String searchKeyword = buildSearchKeyword(req);
    List<Disease> relatedDiseases = searchRelatedDiseases(req);
    String knowledgeContext = relatedDiseases.stream()
        .map(d -> String.format("【%s】\n症状：%s\n病因：%s\n治疗：%s", 
            d.getName(), d.getSymptomsSummary(), d.getCauses(), d.getTreatment()))
        .collect(Collectors.joining("\n\n"));
    List<RagDocument> ragReferences = ragSearchService.search(buildRagQuery(req, relatedDiseases), 5);
    String ragContext = ragSearchService.buildContext(ragReferences);

    // 2. 搜索相关药剂推荐
    List<Product> relatedProducts = new ArrayList<>();
    if (searchKeyword != null) {
      Set<String> terms = buildSearchTerms(req, relatedDiseases);
      relatedProducts = productRepo.findAll().stream()
          .filter(p -> p.getStatus() == ProductStatus.ON)
          .filter(p -> matchesProduct(p, terms))
          .limit(3)
          .collect(Collectors.toList());
    }

    String imageUrl = saveDiagnoseImage(req);

    if (baseUrl == null || baseUrl.isBlank()) {
      return ApiResponse.ok(buildFallbackResponse(authUser, req, relatedDiseases, relatedProducts, ragReferences, imageUrl, "AI 服务暂未配置"));
    }

    String systemPrompt = """
        你是一位名为“水产智诊平台”的资深水产养殖与病害专家。
        你拥有专业的水产病害知识库支持，能够精准分析各种水产动物的疾病。
        
        """;
    
    if (!knowledgeContext.isBlank()) {
      systemPrompt += "以下是从专业知识库中检索到的参考信息，请优先结合这些信息进行诊断：\n" + knowledgeContext + "\n\n";
    }

    if (!ragContext.isBlank()) {
      systemPrompt += "以下是 RAG 资料库召回的资料片段。请把它们作为辅助证据，输出时不要编造未出现的来源：\n" + ragContext + "\n\n";
    }

    if (!relatedProducts.isEmpty()) {
      String productInfo = relatedProducts.stream()
          .map(p -> String.format("- %s (ID:%d): %s", p.getName(), p.getId(), p.getDescription()))
          .collect(Collectors.joining("\n"));
      systemPrompt += "平台水产优选商城中有以下对症药剂，请在处理建议中提及它们（只需提及名称，系统会自动生成购买链接）：\n" + productInfo + "\n\n";
    }

    systemPrompt += """
        当用户提供鱼类症状以及环境信息时，请输出一份像真实水产问诊系统的结构化诊断报告，必须包含：
        1. 【风险分级】：低/中/高，并说明是否存在急性缺氧、大量死亡、强传染等紧急风险。
        2. 【初步诊断】：给出1-3个可能病害，按可能性排序，不确定时明确说明还需观察或检测。
        3. 【诊断依据】：逐条对应用户描述、鱼种、水温、水质、发病时长和知识库证据。
        4. 【立即处理】：先隔离、增氧、停食/减料、换水比例、水质检测等可操作步骤。
        5. 【用药与剂量提醒】：如提及药剂，必须强调按产品说明和当地规范使用，避免超量、混用和休药期风险。
        6. 【后续观察】：列出24-72小时内要复查的指标。
        7. 【预防措施】：给出养殖管理、消毒、投喂、水质维护建议。
        8. 【参考资料】：列出本次使用到的 RAG 资料标题或知识库条目名称。
        
        准确性要求：
        - 只能基于用户信息、病害知识库和 RAG 资料作出判断，信息不足时必须写“疑似”并说明还需要补充什么。
        - 不要编造不存在的病名、商品、检测结果、来源或剂量。
        - 病毒病不要承诺特效药；食用鱼用药必须提醒遵守标签、当地规范和休药期。
        - 先处理急性风险：缺氧、大量死亡、强传染、急性水质异常。
        - 若当前模型未实际分析图片，只能说明图片已归档，不要声称从图片中观察到了具体病灶。
        
        请使用专业、谨慎、权威且富有同理心的中文回答。不要声称替代兽医/水产技术员现场诊断。
        """;

    List<Map<String, Object>> messages;
    boolean canUseVision = supportsVision(props.getAi().getModel());
    if (req.getImageBase64() != null && !req.getImageBase64().isBlank() && canUseVision) {
      String mime = normalizeImageMimeType(req.getImageMimeType());
      String userText = "请结合这张鱼类病灶图片和以下问诊信息进行诊断。\n" +
          (req.getDescription() != null ? req.getDescription() : "");
      messages = List.of(
          Map.of("role", "system", "content", systemPrompt),
          Map.of("role", "user", "content", List.of(
              Map.of("type", "text", "text", userText),
              Map.of("type", "image_url", "image_url", Map.of("url", "data:" + mime + ";base64," + req.getImageBase64()))
          ))
      );
    } else {
      messages = List.of(
          Map.of("role", "system", "content", systemPrompt),
          Map.of("role", "user", "content", buildUserPrompt(req, imageUrl, canUseVision))
      );
    }

    Map<String, Object> body = Map.of(
        "model", props.getAi().getModel(),
        "messages", messages,
        "stream", false
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (apiKey != null && !apiKey.isBlank()) {
      headers.setBearerAuth(apiKey);
    }

    try {
      ResponseEntity<Map> response = restTemplate.exchange(
          baseUrl + "/chat/completions",
          HttpMethod.POST,
          new HttpEntity<>(body, headers),
          Map.class
      );
      Map<?, ?> respBody = response.getBody();
      if (respBody != null && respBody.containsKey("choices")) {
        List<?> choices = (List<?>) respBody.get("choices");
        if (!choices.isEmpty()) {
          Map<?, ?> choice = (Map<?, ?>) choices.get(0);
          Map<?, ?> message = (Map<?, ?>) choice.get("message");
          String content = (String) message.get("content");
          
          boolean saved = false;
          if (authUser != null) {
            saveRecord(authUser.getId(), req, content, imageUrl, relatedDiseases, ragReferences);
            saved = true;
          }

          DiagnoseResponse data = new DiagnoseResponse(
              content,
              saved,
              imageUrl,
              relatedDiseases.stream().map(DiseaseListItemDto::from).toList(),
              relatedProducts.stream().map(ProductListItemDto::from).toList(),
              ragReferences.stream().map(RagDocumentDto::from).toList()
          );
          return ApiResponse.ok(data);
        }
      }
      return ApiResponse.ok(buildFallbackResponse(authUser, req, relatedDiseases, relatedProducts, ragReferences, imageUrl, "AI 返回数据格式异常"));
    } catch (Exception e) {
      return ApiResponse.ok(buildFallbackResponse(authUser, req, relatedDiseases, relatedProducts, ragReferences, imageUrl, "AI 服务调用失败：" + e.getMessage()));
    }
  }

  @GetMapping("/history")
  public ApiResponse<Page<DiagnoseRecordDto>> history(@AuthenticationPrincipal AuthUser authUser,
                                                      @RequestParam(defaultValue = "0") int page) {
    if (authUser == null) return ApiResponse.error("未登录");
    Pageable pr = PageRequest.of(page, 10);
    return ApiResponse.ok(recordRepo.findByUserIdOrderByCreatedAtDesc(authUser.getId(), pr).map(DiagnoseRecordDto::from));
  }

  private void saveRecord(Long userId, DiagnoseRequest req, String result, String imageUrl,
                          List<Disease> diseases, List<RagDocument> references) {
    User user = userRepo.findById(userId).orElse(null);
    if (user == null) return;
    
    DiagnoseRecord record = new DiagnoseRecord();
    record.setUser(user);
    record.setDescription(req.getDescription());
    record.setResult(result);
    record.setSpecies(req.getSpecies());
    record.setTemperature(req.getTemperature());
    record.setWaterQuality(req.getWaterQuality());
    record.setDuration(req.getDuration());
    record.setImageUrl(imageUrl);
    record.setConfidence(diseases.isEmpty() ? "LOW" : "MEDIUM");
    record.setEvidenceJson(toJson(diseases.stream().map(Disease::getName).toList()));
    record.setSourcesJson(toJson(references.stream().map(RagDocument::getTitle).toList()));
    record.setWarningsJson(toJson(List.of("结果仅用于学习和辅助判断，严重情况请联系水产技术人员或兽医")));
    record.setNextStepsJson(toJson(List.of("复查水温、溶氧、pH、氨氮和亚硝酸盐", "记录24至72小时症状变化")));
    recordRepo.save(record);
  }

  private String toJson(Object value) {
    try { return objectMapper.writeValueAsString(value); }
    catch (Exception ignored) { return "[]"; }
  }

  private DiagnoseResponse buildFallbackResponse(AuthUser authUser,
                                                 DiagnoseRequest req,
                                                 List<Disease> relatedDiseases,
                                                 List<Product> relatedProducts,
                                                 List<RagDocument> ragReferences,
                                                 String imageUrl,
                                                 String reason) {
    String report = buildFallbackReport(req, relatedDiseases, relatedProducts, ragReferences, reason);
    boolean saved = false;
    if (authUser != null) {
      saveRecord(authUser.getId(), req, report, imageUrl, relatedDiseases, ragReferences);
      saved = true;
    }
    return new DiagnoseResponse(
        report,
        saved,
        imageUrl,
        relatedDiseases.stream().map(DiseaseListItemDto::from).toList(),
        relatedProducts.stream().map(ProductListItemDto::from).toList(),
        ragReferences.stream().map(RagDocumentDto::from).toList()
    );
  }

  private String buildFallbackReport(DiagnoseRequest req,
                                     List<Disease> relatedDiseases,
                                     List<Product> relatedProducts,
                                     List<RagDocument> ragReferences,
                                     String reason) {
    StringBuilder report = new StringBuilder();
    report.append("【风险分级】：中\n");
    report.append("当前由知识库辅助诊断生成，原因：").append(reason).append("。若出现大量死亡、明显浮头、鳃部发黑或水体恶臭，请立即增氧、暂停投喂并联系当地水产技术员现场处理。\n\n");

    report.append("【初步诊断】：\n");
    if (relatedDiseases.isEmpty()) {
      report.append("暂未匹配到明确病害。建议补充鱼种、发病时长、水温、pH、氨氮、亚硝酸盐、死亡数量和体表/鳃部照片特征。\n\n");
    } else {
      for (int i = 0; i < relatedDiseases.size(); i++) {
        Disease d = relatedDiseases.get(i);
        report.append(i + 1).append(". ").append(d.getName());
        if (d.getDiseaseType() != null && !d.getDiseaseType().isBlank()) {
          report.append("（").append(d.getDiseaseType()).append("）");
        }
        report.append("\n");
      }
      report.append("\n");
    }

    report.append("【诊断依据】：\n");
    appendInputLine(report, "鱼种", req.getSpecies());
    appendInputLine(report, "症状描述", req.getDescription());
    appendInputLine(report, "水温", req.getTemperature());
    appendInputLine(report, "发病时长", req.getDuration());
    appendInputLine(report, "水质情况", req.getWaterQuality());
    if (!relatedDiseases.isEmpty()) {
      report.append("知识库匹配到的主要症状：").append(relatedDiseases.stream()
          .map(Disease::getSymptomsSummary)
          .filter(s -> s != null && !s.isBlank())
          .limit(2)
          .collect(Collectors.joining("；"))).append("\n");
    }

    report.append("\n【立即处理】：\n");
    report.append("1. 先增强增氧，观察鱼是否继续浮头或离群。\n");
    report.append("2. 暂停或减少投喂 24 小时，避免水质继续恶化。\n");
    report.append("3. 检测 pH、氨氮、亚硝酸盐和溶氧；如指标异常，少量多次换水并稳定水温。\n");
    report.append("4. 对病鱼进行隔离观察，记录死亡数量、病灶扩散速度和摄食变化。\n");

    report.append("\n【用药与剂量提醒】：\n");
    if (relatedProducts.isEmpty()) {
      report.append("暂未匹配到合适商品。不要盲目混用抗菌、杀虫、消毒类药剂，先确认病因类型。\n");
    } else {
      report.append("可查看平台推荐用品：").append(relatedProducts.stream().map(Product::getName).collect(Collectors.joining("、"))).append("。用药必须按产品说明和当地规范执行，注意休药期和水体容量，避免超量。\n");
    }

    report.append("\n【后续观察】：\n");
    report.append("24-72 小时内重点复查：浮头是否缓解、体表病灶是否扩大、摄食是否恢复、死亡数量是否下降、水质指标是否稳定。\n");

    report.append("\n【预防措施】：\n");
    report.append("保持水体稳定，定期检测关键水质指标；新鱼入池先隔离检疫；换水、转运、消毒和用药时避免温差和应激过大。\n");

    if (!ragReferences.isEmpty()) {
      report.append("\n【参考资料】：\n");
      ragReferences.stream().limit(5).forEach(doc ->
          report.append("- ").append(doc.getTitle())
              .append("（").append(doc.getSourceName() == null ? "本地资料" : doc.getSourceName()).append("）\n"));
    }
    return report.toString();
  }

  private String saveDiagnoseImage(DiagnoseRequest req) {
    if (req.getImageBase64() == null || req.getImageBase64().isBlank()) return null;
    try {
      String mime = normalizeImageMimeType(req.getImageMimeType());
      String ext = switch (mime) {
        case "image/png" -> "png";
        case "image/webp" -> "webp";
        default -> "jpg";
      };
      byte[] bytes = Base64.getDecoder().decode(req.getImageBase64());
      Path dir = Path.of(props.getDiagnoseImage().getDir()).toAbsolutePath().normalize();
      Files.createDirectories(dir);
      String filename = Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
      Path target = dir.resolve(filename).normalize();
      if (!target.startsWith(dir)) return null;
      Files.write(target, bytes);
      return "/diagnose-images/" + filename;
    } catch (Exception e) {
      return null;
    }
  }

  private String normalizeImageMimeType(String mimeType) {
    if (mimeType == null) return "image/jpeg";
    String mime = mimeType.toLowerCase();
    if (mime.equals("image/png") || mime.equals("image/webp") || mime.equals("image/jpeg")) {
      return mime;
    }
    return "image/jpeg";
  }

  private boolean supportsVision(String model) {
    if (model == null) return false;
    String name = model.toLowerCase();
    return name.contains("vision") || name.contains("vl") || name.contains("gpt-4o") || name.contains("qwen-vl");
  }

  private String buildUserPrompt(DiagnoseRequest req, String imageUrl, boolean imageAnalyzed) {
    StringBuilder prompt = new StringBuilder();
    appendInputLine(prompt, "症状描述", req.getDescription());
    appendInputLine(prompt, "鱼种", req.getSpecies());
    appendInputLine(prompt, "水温", req.getTemperature());
    appendInputLine(prompt, "发病时长", req.getDuration());
    appendInputLine(prompt, "水质情况", req.getWaterQuality());
    if (imageUrl != null && !imageUrl.isBlank()) {
      prompt.append("- 病灶图片：已上传并归档");
      if (!imageAnalyzed) {
        prompt.append("，当前模型不支持图片识别，本次仅按文字信息和 RAG 资料诊断");
      }
      prompt.append("\n");
    }
    return prompt.isEmpty() ? "用户仅上传了图片，请根据可用信息谨慎给出补充问诊建议。" : prompt.toString();
  }

  private void appendInputLine(StringBuilder report, String label, String value) {
    if (value != null && !value.isBlank()) {
      report.append("- ").append(label).append("：").append(value).append("\n");
    }
  }

  private String buildSearchKeyword(DiagnoseRequest req) {
    LinkedHashSet<String> terms = new LinkedHashSet<>();
    addSymptomTerms(terms, req.getDescription());
    addSymptomTerms(terms, req.getWaterQuality());
    addIfPresent(terms, req.getSpecies());
    return terms.stream().findFirst().orElse("");
  }

  private List<Disease> searchRelatedDiseases(DiagnoseRequest req) {
    LinkedHashSet<Disease> result = new LinkedHashSet<>();
    Set<String> terms = buildSearchTerms(req, List.of());
    if (terms.isEmpty()) addIfPresent(terms, buildSearchKeyword(req));

    for (String term : terms) {
      diseaseRepo.search(term, null, PageRequest.of(0, 5)).getContent().forEach(result::add);
      if (result.size() >= 5) break;
    }
    if (result.isEmpty()) {
      diseaseRepo.search(buildSearchKeyword(req), null, PageRequest.of(0, 5)).getContent().forEach(result::add);
    }
    return result.stream().limit(5).toList();
  }

  private String buildRagQuery(DiagnoseRequest req, List<Disease> diseases) {
    StringBuilder query = new StringBuilder();
    appendQueryPart(query, req.getDescription());
    appendQueryPart(query, req.getSpecies());
    appendQueryPart(query, req.getTemperature());
    appendQueryPart(query, req.getDuration());
    appendQueryPart(query, req.getWaterQuality());
    diseases.stream().limit(3).forEach(d -> {
      appendQueryPart(query, d.getName());
      appendQueryPart(query, d.getDiseaseType());
      appendQueryPart(query, d.getAffectedSpecies());
    });
    return query.toString();
  }

  private void appendQueryPart(StringBuilder query, String value) {
    if (value != null && !value.isBlank()) {
      if (!query.isEmpty()) query.append(' ');
      query.append(value.trim());
    }
  }

  private Set<String> buildSearchTerms(DiagnoseRequest req, List<Disease> diseases) {
    LinkedHashSet<String> terms = new LinkedHashSet<>();
    addSymptomTerms(terms, req.getDescription());
    addSymptomTerms(terms, req.getWaterQuality());
    addIfPresent(terms, req.getSpecies());
    diseases.forEach(d -> addIfPresent(terms, d.getName()));
    return terms;
  }

  private void addSymptomTerms(Set<String> terms, String text) {
    if (text == null || text.isBlank()) return;
    List<String> known = Arrays.asList(
        "白点", "白毛", "水霉", "烂鳍", "腐烂", "掉鳞", "竖鳞", "腹部膨大", "充血", "出血",
        "浮头", "缺氧", "鳃", "黏液", "溃疡", "寄生虫", "不吃食", "食欲", "打转", "离群",
        "眼球突出", "氨氮", "亚硝酸盐", "草鱼", "锦鲤", "金鱼", "对虾", "加州鲈",
        "蹭缸", "红斑", "烂鳃", "腹水", "棉絮", "白膜", "换水", "死亡", "高温", "低温"
    );
    known.stream().filter(text::contains).forEach(terms::add);
    if (terms.isEmpty()) {
      String compact = text.replaceAll("\\s+", "");
      terms.add(compact.length() > 12 ? compact.substring(0, 12) : compact);
    }
  }

  private void addIfPresent(Set<String> terms, String value) {
    if (value != null && !value.isBlank()) {
      terms.add(value.trim());
    }
  }

  private boolean matchesProduct(Product product, Set<String> terms) {
    String haystack = String.join(" ",
        product.getName() == null ? "" : product.getName(),
        product.getDescription() == null ? "" : product.getDescription(),
        product.getSymptomTags() == null ? "" : product.getSymptomTags(),
        product.getUsageAdvice() == null ? "" : product.getUsageAdvice()
    );
    return terms.stream().anyMatch(haystack::contains);
  }
}
