package com.fishmart.guestbook;

import com.fishmart.common.ApiResponse;
import com.fishmart.common.BizException;
import com.fishmart.content.ContentStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/admin/guestbook")
@PreAuthorize("@guestbookManagerGuard.canManage(authentication)")
public class AdminGuestbookController {
  private final GuestbookRepository repository;

  public AdminGuestbookController(GuestbookRepository repository) { this.repository = repository; }

  @GetMapping
  public ApiResponse<Page<GuestbookDto>> list(@RequestParam(defaultValue = "PENDING") String status,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
    ContentStatus parsed = parse(status);
    PageRequest request = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
        Sort.by(Sort.Direction.DESC, "createdAt"));
    return ApiResponse.ok(repository.findByStatusOrderByCreatedAtDesc(parsed, request)
        .map(item -> GuestbookDto.from(item, true)));
  }

  @PatchMapping("/{id}")
  @Transactional
  public ApiResponse<GuestbookDto> review(@PathVariable Long id, @Valid @RequestBody GuestbookReviewRequest request) {
    GuestbookMessage item = repository.findById(id).orElseThrow(() -> new BizException("留言不存在"));
    if (request.status() != null) {
      ContentStatus status = parse(request.status());
      if (status == ContentStatus.PENDING) throw new BizException("审核结果不能为待审核");
      item.setStatus(status);
    }
    if (request.author() != null) item.setAuthorName(request.author().isBlank() ? "匿名用户" : request.author().trim());
    if (request.content() != null) {
      if (request.content().isBlank()) throw new BizException("留言内容不能为空");
      item.setContent(request.content().trim());
    }
    if (request.reply() != null) item.setAdminReply(request.reply().isBlank() ? null : request.reply().trim());
    item.setReviewedAt(Instant.now());
    return ApiResponse.ok(GuestbookDto.from(repository.save(item), true));
  }

  @DeleteMapping("/{id}")
  @Transactional
  public ApiResponse<Void> delete(@PathVariable Long id) {
    if (!repository.existsById(id)) throw new BizException("留言不存在");
    repository.deleteById(id);
    return ApiResponse.ok(null);
  }

  private ContentStatus parse(String status) {
    try { return ContentStatus.valueOf(status.toUpperCase()); }
    catch (Exception ignored) { throw new BizException("非法审核状态"); }
  }
}
