package com.fishmart.print;

import com.fishmart.common.ApiResponse;
import com.fishmart.common.BizException;
import com.fishmart.config.AppProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/print")
public class PrintFileController {
  private final AppProperties props;

  public PrintFileController(AppProperties props) {
    this.props = props;
  }

  @GetMapping("/files")
  public ApiResponse<List<Map<String, Object>>> files() throws IOException {
    Path dir = printDir();
    Files.createDirectories(dir);

    try (var stream = Files.list(dir)) {
      List<Map<String, Object>> files = stream
          .filter(Files::isRegularFile)
          .map(this::toFileInfo)
          .sorted(Comparator.comparing((Map<String, Object> item) -> (Long) item.get("lastModified")).reversed())
          .toList();
      return ApiResponse.ok(files);
    }
  }

  @PostMapping("/upload")
  public ApiResponse<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new BizException("文件不能为空");
    }

    Path dir = printDir();
    Files.createDirectories(dir);

    String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
    String safeName = original.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    if (safeName.isBlank() || safeName.contains("..")) {
      safeName = "file";
    }

    String filename = Instant.now().toEpochMilli() + "-" + safeName;
    Path target = dir.resolve(filename).normalize();
    if (!target.startsWith(dir)) {
      throw new BizException("文件名不合法");
    }

    file.transferTo(target);
    return ApiResponse.ok(toFileInfo(target));
  }

  private Path printDir() {
    return Path.of(props.getPrint().getDir()).toAbsolutePath().normalize();
  }

  private Map<String, Object> toFileInfo(Path file) {
    try {
      String filename = file.getFileName().toString();
      String encoded = UriUtils.encodePathSegment(filename, StandardCharsets.UTF_8);
      return Map.of(
          "name", filename,
          "url", "/print-files/" + encoded,
          "size", Files.size(file),
          "lastModified", Files.getLastModifiedTime(file).toMillis()
      );
    } catch (IOException e) {
      throw new IllegalStateException("读取文件信息失败", e);
    }
  }
}
