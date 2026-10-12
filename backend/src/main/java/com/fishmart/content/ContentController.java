package com.fishmart.content;

import com.fishmart.common.ApiResponse;
import com.fishmart.content.dto.*;
import com.fishmart.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ContentController {
  private final ContentService contentService;
  private final LikeRepository likeRepo;
  private final FavoriteRepository favRepo;

  public ContentController(ContentService contentService, LikeRepository likeRepo, FavoriteRepository favRepo) {
    this.contentService = contentService;
    this.likeRepo = likeRepo;
    this.favRepo = favRepo;
  }

  @GetMapping("/articles")
  public ApiResponse<Page<ArticleDto>> articles(@RequestParam(required = false) String q,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
    PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "id"));
    return ApiResponse.ok(contentService.listArticles(q, pr));
  }

  @GetMapping("/articles/{id}")
  public ApiResponse<ArticleDto> article(@PathVariable Long id) {
    return ApiResponse.ok(contentService.getArticle(id));
  }

  @GetMapping("/posts")
  public ApiResponse<Page<PostDto>> posts(@RequestParam(required = false) String q,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
    PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "id"));
    return ApiResponse.ok(contentService.listPosts(q, pr));
  }

  @PostMapping("/posts")
  public ApiResponse<PostDto> createPost(Authentication authentication, @Valid @RequestBody CreatePostRequest req) {
    AuthUser u = (AuthUser) authentication.getPrincipal();
    return ApiResponse.ok(contentService.createPost(u.getId(), req));
  }

  @GetMapping("/posts/{id}")
  public ApiResponse<PostDto> post(@PathVariable Long id) {
    return ApiResponse.ok(contentService.getPost(id));
  }

  @GetMapping("/comments")
  public ApiResponse<Page<CommentDto>> comments(@RequestParam TargetType targetType,
                                                @RequestParam Long targetId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
    PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "id"));
    return ApiResponse.ok(contentService.listComments(targetType, targetId, pr));
  }

  @PostMapping("/articles/{id}/comments")
  public ApiResponse<CommentDto> commentArticle(Authentication authentication, @PathVariable Long id, @Valid @RequestBody CreateCommentRequest req) {
    AuthUser u = (AuthUser) authentication.getPrincipal();
    return ApiResponse.ok(contentService.addComment(u.getId(), TargetType.ARTICLE, id, req));
  }

  @PostMapping("/posts/{id}/comments")
  public ApiResponse<CommentDto> commentPost(Authentication authentication, @PathVariable Long id, @Valid @RequestBody CreateCommentRequest req) {
    AuthUser u = (AuthUser) authentication.getPrincipal();
    return ApiResponse.ok(contentService.addComment(u.getId(), TargetType.POST, id, req));
  }

  @PostMapping("/likes")
  public ApiResponse<Map<String, Object>> toggleLike(Authentication authentication,
                                                    @RequestParam TargetType targetType,
                                                    @RequestParam Long targetId) {
    AuthUser u = (AuthUser) authentication.getPrincipal();
    boolean liked = contentService.toggleLike(u.getId(), targetType, targetId);
    long count = likeRepo.countByTargetTypeAndTargetId(targetType, targetId);
    return ApiResponse.ok(Map.of("liked", liked, "count", count));
  }

  @PostMapping("/favorites")
  public ApiResponse<Map<String, Object>> toggleFavorite(Authentication authentication,
                                                        @RequestParam TargetType targetType,
                                                        @RequestParam Long targetId) {
    AuthUser u = (AuthUser) authentication.getPrincipal();
    boolean favored = contentService.toggleFavorite(u.getId(), targetType, targetId);
    long count = favRepo.countByTargetTypeAndTargetId(targetType, targetId);
    return ApiResponse.ok(Map.of("favored", favored, "count", count));
  }

  @GetMapping("/favorites")
  public ApiResponse<Page<FavoriteItemDto>> favorites(Authentication authentication,
                                                       @RequestParam(required = false) TargetType targetType,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "12") int size) {
    AuthUser u = (AuthUser) authentication.getPrincipal();
    PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
    return ApiResponse.ok(contentService.listFavorites(u.getId(), targetType, pr));
  }

  @GetMapping("/engagement")
  public ApiResponse<Map<Long, EngagementDto>> engagement(Authentication authentication,
                                                           @RequestParam TargetType targetType,
                                                           @RequestParam List<Long> targetIds) {
    Long userId = authentication != null && authentication.getPrincipal() instanceof AuthUser u ? u.getId() : null;
    return ApiResponse.ok(contentService.engagement(targetType, targetIds, userId));
  }
}

