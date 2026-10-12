package com.fishmart.diagnose;

import com.fishmart.config.AppProperties;
import com.fishmart.security.AuthUser;
import com.fishmart.user.RoleName;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
public class DiagnoseImageController {
  private final DiagnoseRecordRepository records;
  private final AppProperties props;

  public DiagnoseImageController(DiagnoseRecordRepository records, AppProperties props) {
    this.records = records; this.props = props;
  }

  @GetMapping("/diagnose-images/{filename:.+}")
  public ResponseEntity<Resource> image(@PathVariable String filename, @AuthenticationPrincipal AuthUser user) throws Exception {
    String imageUrl = "/diagnose-images/" + filename;
    DiagnoseRecord record = records.findByImageUrl(imageUrl).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    boolean admin = user != null && user.getRoles().contains(RoleName.ADMIN);
    if (user == null || (!admin && !record.getUser().getId().equals(user.getId()))) return ResponseEntity.status(403).build();
    Path root = Path.of(props.getDiagnoseImage().getDir()).toAbsolutePath().normalize();
    Path file = root.resolve(filename).normalize();
    if (!file.startsWith(root) || !Files.isRegularFile(file)) return ResponseEntity.notFound().build();
    String contentType = Files.probeContentType(file);
    Resource resource = new UrlResource(file.toUri());
    return ResponseEntity.ok()
        .contentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
        .body(resource);
  }
}
