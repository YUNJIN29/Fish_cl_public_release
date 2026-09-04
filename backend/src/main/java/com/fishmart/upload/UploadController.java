package com.fishmart.upload;

import com.fishmart.common.ApiResponse;
import com.fishmart.common.BizException;
import com.fishmart.config.AppProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UploadController {
  private final AppProperties props;

  public UploadController(AppProperties props) {
    this.props = props;
  }

  @PostMapping("/upload")
  public ApiResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new BizException("文件不能为空");
    }
    String original = file.getOriginalFilename();
    String ext = StringUtils.getFilenameExtension(original == null ? "" : original);
    String safeExt = (ext == null || ext.isBlank()) ? "bin" : ext.toLowerCase();
    if (safeExt.length() > 10) safeExt = "bin";

    Path dir = Path.of(props.getUpload().getDir()).toAbsolutePath().normalize();
    Files.createDirectories(dir);

    String filename = Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().replace("-", "") + "." + safeExt;
    Path target = dir.resolve(filename).normalize();
    file.transferTo(target);

    String url = "/uploads/" + filename;
    return ApiResponse.ok(Map.of("url", url));
  }
}

