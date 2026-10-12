package com.fishmart.upload;

import com.fishmart.common.ApiResponse;
import com.fishmart.config.AppProperties;
import com.fishmart.user.User;
import com.fishmart.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/admin/uploads")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUploadController {
  private final UploadResourceRepository resources;
  private final UserRepository users;
  private final AppProperties props;

  public AdminUploadController(UploadResourceRepository resources, UserRepository users, AppProperties props) {
    this.resources = resources; this.users = users; this.props = props;
  }

  @GetMapping
  @Transactional(readOnly = true)
  public ApiResponse<Page<AdminUploadDto>> list(@RequestParam(required = false) String q,
                                                @RequestParam(required = false) String kind,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
    PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<UploadResource> result = resources.search(normalize(q), normalizeKind(kind), pageable);
    Map<Long, User> owners = users.findAllById(result.getContent().stream().map(UploadResource::getOwnerUserId).distinct().toList())
        .stream().collect(Collectors.toMap(User::getId, Function.identity()));
    var content = result.getContent().stream().map(item -> {
      User owner = owners.get(item.getOwnerUserId());
      return new AdminUploadDto(item.getId(), item.getOriginalName(), item.getContentType(), item.getSizeBytes(), item.getCreatedAt(), item.getOwnerUserId(), owner == null ? "已删除用户" : owner.getUsername(), "/api/resources/" + item.getId());
    }).toList();
    return ApiResponse.ok(new PageImpl<>(content, pageable, result.getTotalElements()));
  }

  @DeleteMapping("/{id}")
  @Transactional
  public ApiResponse<Map<String, Object>> delete(@PathVariable String id) throws Exception {
    UploadResource resource = resources.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    Path root = Path.of(props.getPrivateUpload().getDir()).toAbsolutePath().normalize();
    Path original = root.resolve(resource.getStoredName()).normalize();
    if (!original.startsWith(root)) throw new IllegalStateException("Invalid stored upload path");
    Path trash = root.resolve(".trash").resolve(resource.getStoredName()).normalize();
    Files.createDirectories(trash.getParent());
    boolean moved = Files.isRegularFile(original);
    if (moved) Files.move(original, trash);
    resources.delete(resource);
    resources.flush();
    if (moved) TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override public void afterCompletion(int status) {
        try {
          if (status == STATUS_COMMITTED) Files.deleteIfExists(trash);
          else if (Files.exists(trash)) Files.move(trash, original);
        } catch (Exception ignored) { /* 保留在 .trash 供运维恢复，避免重新公开不一致文件 */ }
      }
    });
    return ApiResponse.ok(Map.of("deleted", true, "id", id));
  }

  private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
  private String normalizeKind(String kind) { return "IMAGE".equalsIgnoreCase(kind) || "DOCUMENT".equalsIgnoreCase(kind) ? kind.toUpperCase() : null; }
}
