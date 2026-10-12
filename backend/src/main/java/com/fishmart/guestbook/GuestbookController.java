package com.fishmart.guestbook;

import com.fishmart.common.ApiResponse;
import com.fishmart.content.ContentStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guestbook")
public class GuestbookController {
  private final GuestbookRepository repository;

  public GuestbookController(GuestbookRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public ApiResponse<Page<GuestbookDto>> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "6") int size) {
    PageRequest request = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 20),
        Sort.by(Sort.Direction.DESC, "createdAt"));
    return ApiResponse.ok(repository.findByStatusOrderByCreatedAtDesc(ContentStatus.APPROVED, request)
        .map(item -> GuestbookDto.from(item, false)));
  }

  @PostMapping
  @Transactional
  public ApiResponse<GuestbookDto> create(@Valid @RequestBody GuestbookRequest request) {
    GuestbookMessage item = new GuestbookMessage();
    item.setAuthorName(request.author().trim());
    item.setContent(request.content().trim());
    return ApiResponse.ok(GuestbookDto.from(repository.save(item), true));
  }
}
