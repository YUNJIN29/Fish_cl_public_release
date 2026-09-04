package com.fishmart.admin;

import com.fishmart.admin.dto.SetStatusRequest;
import com.fishmart.common.ApiResponse;
import com.fishmart.common.BizException;
import com.fishmart.content.*;
import com.fishmart.content.dto.CommentDto;
import com.fishmart.content.dto.PostDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/review")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {
  private final PostRepository postRepo;
  private final CommentRepository commentRepo;
  private final ArticleRepository articleRepo;

  public AdminReviewController(PostRepository postRepo, CommentRepository commentRepo, ArticleRepository articleRepo) {
    this.postRepo = postRepo;
    this.commentRepo = commentRepo;
    this.articleRepo = articleRepo;
  }

  @GetMapping("/posts")
  public ApiResponse<Page<PostDto>> pendingPosts(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
    PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "id"));
    return ApiResponse.ok(postRepo.search(null, ContentStatus.PENDING, pr).map(p -> PostDto.from(p, true)));
  }

  @PatchMapping("/posts/{id}")
  @Transactional
  public ApiResponse<PostDto> reviewPost(@PathVariable Long id, @Valid @RequestBody SetStatusRequest req) {
    ContentStatus st = parse(req.getStatus());
    if (st == ContentStatus.PENDING) throw new BizException("状态不允许设置为 PENDING");
    Post p = postRepo.findById(id).orElseThrow(() -> new BizException("帖子不存在"));
    p.setStatus(st);
    return ApiResponse.ok(PostDto.from(postRepo.save(p), true));
  }

  @GetMapping("/comments")
  public ApiResponse<Page<CommentDto>> pendingComments(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
    PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "id"));
    return ApiResponse.ok(commentRepo.findByStatusOrderByIdDesc(ContentStatus.PENDING, pr).map(CommentDto::from));
  }

  @PatchMapping("/comments/{id}")
  @Transactional
  public ApiResponse<CommentDto> reviewComment(@PathVariable Long id, @Valid @RequestBody SetStatusRequest req) {
    ContentStatus st = parse(req.getStatus());
    if (st == ContentStatus.PENDING) throw new BizException("状态不允许设置为 PENDING");
    Comment c = commentRepo.findById(id).orElseThrow(() -> new BizException("评论不存在"));
    c.setStatus(st);
    return ApiResponse.ok(CommentDto.from(commentRepo.save(c)));
  }

  @GetMapping("/articles")
  public ApiResponse<Page<Article>> pendingArticles(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
    PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "id"));
    return ApiResponse.ok(articleRepo.search(null, ContentStatus.PENDING, pr));
  }

  @PatchMapping("/articles/{id}")
  @Transactional
  public ApiResponse<Article> reviewArticle(@PathVariable Long id, @Valid @RequestBody SetStatusRequest req) {
    ContentStatus st = parse(req.getStatus());
    if (st == ContentStatus.PENDING) throw new BizException("状态不允许设置为 PENDING");
    Article a = articleRepo.findById(id).orElseThrow(() -> new BizException("文章不存在"));
    a.setStatus(st);
    return ApiResponse.ok(articleRepo.save(a));
  }

  private ContentStatus parse(String s) {
    try {
      return ContentStatus.valueOf(s);
    } catch (Exception e) {
      throw new BizException("非法状态: " + s);
    }
  }
}

