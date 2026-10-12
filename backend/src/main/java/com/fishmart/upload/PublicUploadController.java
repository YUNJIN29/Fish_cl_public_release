package com.fishmart.upload;

import com.fishmart.config.AppProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

@RestController
public class PublicUploadController {
  private static final Pattern SITE_ASSET = Pattern.compile("^(?:real_)?(?:fish|aqua|banner)_\\d+\\.(?:jpg|jpeg|png|webp|gif|svg)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern PORTFOLIO_ASSET = Pattern.compile("^[0-9]{10,}-[a-f0-9]{32}\\.(?:jpg|jpeg|png|webp)$", Pattern.CASE_INSENSITIVE);
  private final AppProperties props;

  public PublicUploadController(AppProperties props) { this.props = props; }

  @GetMapping("/uploads/{filename:.+}")
  public ResponseEntity<Resource> siteAsset(@PathVariable String filename) throws Exception {
    if (!SITE_ASSET.matcher(filename).matches()) return ResponseEntity.notFound().build();
    return serve(Path.of(props.getUpload().getDir()), filename);
  }

  @GetMapping("/uploads/portfolio/{filename:.+}")
  public ResponseEntity<Resource> portfolioAsset(@PathVariable String filename) throws Exception {
    if (!PORTFOLIO_ASSET.matcher(filename).matches()) return ResponseEntity.notFound().build();
    return serve(Path.of(props.getUpload().getDir(), "portfolio"), filename);
  }

  private ResponseEntity<Resource> serve(Path configuredRoot, String filename) throws Exception {
    Path root = configuredRoot.toAbsolutePath().normalize();
    Path file = root.resolve(filename).normalize();
    if (!file.startsWith(root) || !Files.isRegularFile(file)) return ResponseEntity.notFound().build();
    String contentType = Files.probeContentType(file);
    return ResponseEntity.ok()
        .contentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
        .header("X-Content-Type-Options", "nosniff")
        .body(new UrlResource(file.toUri()));
  }
}
