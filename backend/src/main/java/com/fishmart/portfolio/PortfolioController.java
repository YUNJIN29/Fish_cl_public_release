package com.fishmart.portfolio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishmart.common.ApiResponse;
import com.fishmart.common.BizException;
import com.fishmart.config.AppProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {
  private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
  private final PortfolioProfileRepository profileRepository;
  private final PortfolioCertificateRepository certificateRepository;
  private final ObjectMapper objectMapper;
  private final AppProperties props;

  public PortfolioController(PortfolioProfileRepository profileRepository,
                             PortfolioCertificateRepository certificateRepository,
                             ObjectMapper objectMapper, AppProperties props) {
    this.profileRepository = profileRepository;
    this.certificateRepository = certificateRepository;
    this.objectMapper = objectMapper;
    this.props = props;
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> getPortfolio() throws IOException {
    Map<String, Object> result = new LinkedHashMap<>();
    Optional<PortfolioProfile> profile = profileRepository.findById(1L);
    result.put("profile", profile.isPresent()
        ? objectMapper.readValue(profile.get().getContentJson(), new TypeReference<Map<String, Object>>() {})
        : Map.of());
    result.put("certificates", certificateRepository.findAllByOrderByCreatedAtDesc());
    return ApiResponse.ok(result);
  }

  @PutMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<Map<String, Object>> updatePortfolio(@RequestBody Map<String, Object> content) throws IOException {
    Map<String, Object> sanitized = new LinkedHashMap<>();
    Map<String, Integer> allowed = Map.ofEntries(
        Map.entry("headline", 80), Map.entry("highlight", 80), Map.entry("intro", 800),
        Map.entry("about", 1200), Map.entry("research", 160), Map.entry("location", 120),
        Map.entry("email", 160), Map.entry("jobIntent", 120), Map.entry("workPreference", 160),
        Map.entry("internship", 1200), Map.entry("paper", 1600), Map.entry("patent", 1600));
    allowed.forEach((key, maxLength) -> {
      Object raw = content.get(key);
      if (raw instanceof String value) {
        String clean = value.strip();
        sanitized.put(key, clean.substring(0, Math.min(clean.length(), maxLength)));
      }
    });
    PortfolioProfile profile = profileRepository.findById(1L).orElseGet(PortfolioProfile::new);
    profile.setContentJson(objectMapper.writeValueAsString(sanitized));
    profile.setUpdatedAt(Instant.now());
    profileRepository.save(profile);
    return ApiResponse.ok(sanitized);
  }

  @PostMapping(value = "/certificates", consumes = "multipart/form-data")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<PortfolioCertificate> uploadCertificate(@RequestParam("file") MultipartFile file,
                                                              @RequestParam String title,
                                                              @RequestParam(defaultValue = "其他证明") String level,
                                                              @RequestParam(defaultValue = "certificate") String type) throws IOException {
    if (file == null || file.isEmpty()) throw new BizException("请选择证书图片");
    if (title == null || title.isBlank()) throw new BizException("请填写证书名称");
    if (title.length() > 200 || level.length() > 50) throw new BizException("证书信息过长");
    String ext = Optional.ofNullable(StringUtils.getFilenameExtension(file.getOriginalFilename())).orElse("").toLowerCase();
    String contentType = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase();
    if (!IMAGE_EXTENSIONS.contains(ext) || !contentType.startsWith("image/")) throw new BizException("仅支持 JPG、PNG 或 WebP 图片");
    Path dir = Path.of(props.getUpload().getDir(), "portfolio").toAbsolutePath().normalize();
    Files.createDirectories(dir);
    String filename = Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
    file.transferTo(dir.resolve(filename));
    PortfolioCertificate certificate = new PortfolioCertificate();
    certificate.setTitle(title.trim());
    certificate.setLevel(level.trim());
    certificate.setType("competition".equals(type) ? "competition" : "certificate");
    certificate.setUrl("/uploads/portfolio/" + filename);
    return ApiResponse.ok(certificateRepository.save(certificate));
  }
}
