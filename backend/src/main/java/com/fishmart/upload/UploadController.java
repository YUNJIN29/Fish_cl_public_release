package com.fishmart.upload;

import com.fishmart.common.ApiResponse;
import com.fishmart.common.BizException;
import com.fishmart.config.AppProperties;
import com.fishmart.security.AuthUser;
import com.fishmart.user.RoleName;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api")
public class UploadController {
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif", "pdf");
  private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif", "application/pdf");
  private final AppProperties props;
  private final UploadResourceRepository resources;

  public UploadController(AppProperties props, UploadResourceRepository resources) {
    this.props = props; this.resources = resources;
  }

  @PostMapping(value = "/upload", consumes = "multipart/form-data")
  public ApiResponse<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                  @AuthenticationPrincipal AuthUser user) throws IOException {
    if (file == null || file.isEmpty()) throw new BizException("文件不能为空");
    if (user == null) throw new ResponseStatusException(FORBIDDEN);
    String original = StringUtils.cleanPath(Optional.ofNullable(file.getOriginalFilename()).orElse("file"));
    String ext = Optional.ofNullable(StringUtils.getFilenameExtension(original)).orElse("").toLowerCase();
    String contentType = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase();
    if (!ALLOWED_EXTENSIONS.contains(ext) || !ALLOWED_TYPES.contains(contentType) || !signatureMatches(file, ext)) {
      throw new BizException("文件内容与类型不匹配，仅支持 JPG、PNG、WebP、GIF 和 PDF");
    }
    Path dir = Path.of(props.getPrivateUpload().getDir()).toAbsolutePath().normalize();
    Files.createDirectories(dir);
    String id = UUID.randomUUID().toString();
    String storedName = id.replace("-", "") + "." + ext;
    Path target = dir.resolve(storedName).normalize();
    if (!target.startsWith(dir)) throw new BizException("文件路径不合法");
    file.transferTo(target);
    UploadResource resource = new UploadResource();
    resource.setId(id); resource.setOwnerUserId(user.getId()); resource.setOriginalName(limit(original, 255));
    resource.setStoredName(storedName); resource.setContentType(contentType); resource.setSizeBytes(file.getSize()); resource.setCreatedAt(Instant.now());
    try { resources.save(resource); }
    catch (RuntimeException e) { Files.deleteIfExists(target); throw e; }
    return ApiResponse.ok(Map.of("resourceId", id, "url", "/api/resources/" + id, "originalName", resource.getOriginalName()));
  }

  @GetMapping("/resources/{id}")
  public ResponseEntity<Resource> resource(@PathVariable String id, @AuthenticationPrincipal AuthUser user) throws Exception {
    UploadResource metadata = resources.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    boolean admin = user != null && user.getRoles().contains(RoleName.ADMIN);
    if (user == null || (!admin && !metadata.getOwnerUserId().equals(user.getId()))) throw new ResponseStatusException(FORBIDDEN);
    Path root = Path.of(props.getPrivateUpload().getDir()).toAbsolutePath().normalize();
    Path file = root.resolve(metadata.getStoredName()).normalize();
    if (!file.startsWith(root) || !Files.isRegularFile(file)) throw new ResponseStatusException(NOT_FOUND);
    boolean inline = metadata.getContentType().startsWith("image/");
    ContentDisposition disposition = (inline ? ContentDisposition.inline() : ContentDisposition.attachment())
        .filename(metadata.getOriginalName(), StandardCharsets.UTF_8).build();
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(metadata.getContentType()))
        .contentLength(metadata.getSizeBytes())
        .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
        .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
        .header("X-Content-Type-Options", "nosniff")
        .body(new UrlResource(file.toUri()));
  }

  private boolean signatureMatches(MultipartFile file, String ext) throws IOException {
    byte[] head;
    try (var input = file.getInputStream()) { head = input.readNBytes(12); }
    return switch (ext) {
      case "jpg", "jpeg" -> head.length >= 3 && (head[0] & 0xff) == 0xff && (head[1] & 0xff) == 0xd8 && (head[2] & 0xff) == 0xff;
      case "png" -> head.length >= 8 && (head[0] & 0xff) == 0x89 && head[1] == 0x50 && head[2] == 0x4e && head[3] == 0x47;
      case "gif" -> head.length >= 6 && new String(head, 0, 6, StandardCharsets.US_ASCII).matches("GIF8[79]a");
      case "webp" -> head.length >= 12 && new String(head, 0, 4, StandardCharsets.US_ASCII).equals("RIFF") && new String(head, 8, 4, StandardCharsets.US_ASCII).equals("WEBP");
      case "pdf" -> head.length >= 5 && new String(head, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-");
      default -> false;
    };
  }

  private String limit(String value, int max) { return value.length() <= max ? value : value.substring(value.length() - max); }
}
