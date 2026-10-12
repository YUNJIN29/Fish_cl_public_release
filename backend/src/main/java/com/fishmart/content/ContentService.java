package com.fishmart.content;

import com.fishmart.common.BizException;
import com.fishmart.content.dto.*;
import com.fishmart.user.User;
import com.fishmart.user.UserRepository;
import com.fishmart.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ContentService {
  private final ArticleRepository articleRepo;
  private final PostRepository postRepo;
  private final CommentRepository commentRepo;
  private final LikeRepository likeRepo;
  private final FavoriteRepository favRepo;
  private final UserRepository userRepo;

  public ContentService(ArticleRepository articleRepo,
                        PostRepository postRepo,
                        CommentRepository commentRepo,
                        LikeRepository likeRepo,
                        FavoriteRepository favRepo,
                        UserRepository userRepo) {
    this.articleRepo = articleRepo;
    this.postRepo = postRepo;
    this.commentRepo = commentRepo;
    this.likeRepo = likeRepo;
    this.favRepo = favRepo;
    this.userRepo = userRepo;
  }

  public Page<ArticleDto> listArticles(String q, Pageable pageable) {
    return articleRepo.search(q, ContentStatus.APPROVED, pageable).map(a -> ArticleDto.from(a, false));
  }

  public ArticleDto getArticle(Long id) {
    Article a = articleRepo.findById(id).orElseThrow(() -> new BizException("文章不存在"));
    if (a.getStatus() != ContentStatus.APPROVED) throw new BizException("文章未发布");
    return ArticleDto.from(a, true);
  }

  public Page<PostDto> listPosts(String q, Pageable pageable) {
    return postRepo.search(q, ContentStatus.APPROVED, pageable).map(p -> PostDto.from(p, false));
  }

  public PostDto getPost(Long id) {
    Post p = postRepo.findById(id).orElseThrow(() -> new BizException("帖子不存在"));
    if (p.getStatus() != ContentStatus.APPROVED) throw new BizException("帖子未通过审核");
    return PostDto.from(p, true);
  }

  @Transactional
  public PostDto createPost(Long userId, CreatePostRequest req) {
    User u = userRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
    if (u.getStatus() == UserStatus.BANNED) throw new BizException("账号已被封禁");

    Post p = new Post();
    p.setUser(u);
    p.setTitle(req.getTitle());
    p.setContent(req.getContent());
    p.setStatus(ContentStatus.PENDING); // 默认走审核，更符合比赛“治理”
    p = postRepo.save(p);
    return PostDto.from(p, true);
  }

  public Page<CommentDto> listComments(TargetType type, Long targetId, Pageable pageable) {
    return commentRepo.findByTargetTypeAndTargetIdAndStatusOrderByIdDesc(type, targetId, ContentStatus.APPROVED, pageable)
        .map(CommentDto::from);
  }

  @Transactional
  public CommentDto addComment(Long userId, TargetType type, Long targetId, CreateCommentRequest req) {
    User u = userRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
    if (u.getStatus() == UserStatus.BANNED) throw new BizException("账号已被封禁");

    // 简单校验 target 存在
    if (type == TargetType.ARTICLE) {
      Article a = articleRepo.findById(targetId).orElseThrow(() -> new BizException("文章不存在"));
      if (a.getStatus() != ContentStatus.APPROVED) throw new BizException("文章未发布");
    } else if (type == TargetType.POST) {
      Post p = postRepo.findById(targetId).orElseThrow(() -> new BizException("帖子不存在"));
      if (p.getStatus() != ContentStatus.APPROVED) throw new BizException("帖子未通过审核");
    } else {
      throw new BizException("不支持的目标类型");
    }

    Comment c = new Comment();
    c.setUser(u);
    c.setTargetType(type);
    c.setTargetId(targetId);
    c.setContent(req.getContent());
    c.setStatus(ContentStatus.PENDING); // 默认走审核
    c = commentRepo.save(c);
    return CommentDto.from(c);
  }

  @Transactional
  public boolean toggleLike(Long userId, TargetType type, Long targetId) {
    return likeRepo.findByUserIdAndTargetTypeAndTargetId(userId, type, targetId)
        .map(exist -> {
          likeRepo.delete(exist);
          return false;
        })
        .orElseGet(() -> {
          User u = userRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
          LikeEntity le = new LikeEntity();
          le.setUser(u);
          le.setTargetType(type);
          le.setTargetId(targetId);
          likeRepo.save(le);
          return true;
        });
  }

  @Transactional
  public boolean toggleFavorite(Long userId, TargetType type, Long targetId) {
    return favRepo.findByUserIdAndTargetTypeAndTargetId(userId, type, targetId)
        .map(exist -> {
          favRepo.delete(exist);
          return false;
        })
        .orElseGet(() -> {
          validatePublishedTarget(type, targetId);
          User u = userRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
          FavoriteEntity fe = new FavoriteEntity();
          fe.setUser(u);
          fe.setTargetType(type);
          fe.setTargetId(targetId);
          favRepo.save(fe);
          return true;
        });
  }

  @Transactional(readOnly = true)
  public Page<FavoriteItemDto> listFavorites(Long userId, TargetType type, Pageable pageable) {
    Page<FavoriteEntity> favorites = type == null
        ? favRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable)
        : favRepo.findByUserIdAndTargetTypeOrderByCreatedAtDesc(userId, type, pageable);
    return favorites.map(this::toFavoriteItem);
  }

  private void validatePublishedTarget(TargetType type, Long targetId) {
    if (type == TargetType.ARTICLE) {
      Article article = articleRepo.findById(targetId).orElseThrow(() -> new BizException("文章不存在"));
      if (article.getStatus() != ContentStatus.APPROVED) throw new BizException("文章未发布");
      return;
    }
    if (type == TargetType.POST) {
      Post post = postRepo.findById(targetId).orElseThrow(() -> new BizException("帖子不存在"));
      if (post.getStatus() != ContentStatus.APPROVED) throw new BizException("帖子未通过审核");
      return;
    }
    throw new BizException("不支持的收藏类型");
  }

  private FavoriteItemDto toFavoriteItem(FavoriteEntity favorite) {
    if (favorite.getTargetType() == TargetType.ARTICLE) {
      return articleRepo.findById(favorite.getTargetId())
          .filter(article -> article.getStatus() == ContentStatus.APPROVED)
          .map(article -> new FavoriteItemDto(favorite.getId(), favorite.getTargetType(), favorite.getTargetId(), article.getTitle(), summarize(article.getContent()), article.getCoverImageUrl(), favorite.getCreatedAt(), article.getCreatedAt(), true, "/fish/articles/" + article.getId()))
          .orElseGet(() -> unavailableFavorite(favorite));
    }
    if (favorite.getTargetType() == TargetType.POST) {
      return postRepo.findById(favorite.getTargetId())
          .filter(post -> post.getStatus() == ContentStatus.APPROVED)
          .map(post -> new FavoriteItemDto(favorite.getId(), favorite.getTargetType(), favorite.getTargetId(), post.getTitle(), summarize(post.getContent()), null, favorite.getCreatedAt(), post.getCreatedAt(), true, "/fish/posts?post=" + post.getId()))
          .orElseGet(() -> unavailableFavorite(favorite));
    }
    return unavailableFavorite(favorite);
  }

  private FavoriteItemDto unavailableFavorite(FavoriteEntity favorite) {
    return new FavoriteItemDto(favorite.getId(), favorite.getTargetType(), favorite.getTargetId(), "内容已下线", "该内容当前不可浏览，你仍可以取消收藏。", null, favorite.getCreatedAt(), null, false, null);
  }

  private String summarize(String content) {
    if (content == null || content.isBlank()) return "暂无摘要";
    String plain = content.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    return plain.length() > 120 ? plain.substring(0, 120) + "…" : plain;
  }

  @Transactional(readOnly = true)
  public Map<Long, EngagementDto> engagement(TargetType type, List<Long> requestedIds, Long userId) {
    List<Long> ids = requestedIds.stream().filter(java.util.Objects::nonNull).filter(id -> id > 0).distinct().limit(50).toList();
    if (ids.isEmpty()) throw new BizException("请选择需要查询的内容");
    Map<Long, Long> likeCounts = likeRepo.countGrouped(type, ids).stream().collect(Collectors.toMap(TargetCountView::getTargetId, TargetCountView::getTotal));
    Map<Long, Long> favoriteCounts = favRepo.countGrouped(type, ids).stream().collect(Collectors.toMap(TargetCountView::getTargetId, TargetCountView::getTotal));
    Set<Long> likedIds = userId == null ? Set.of() : likeRepo.findByUserIdAndTargetTypeAndTargetIdIn(userId, type, ids).stream().map(LikeEntity::getTargetId).collect(Collectors.toSet());
    Set<Long> favoredIds = userId == null ? Set.of() : favRepo.findByUserIdAndTargetTypeAndTargetIdIn(userId, type, ids).stream().map(FavoriteEntity::getTargetId).collect(Collectors.toSet());
    Map<Long, EngagementDto> result = new LinkedHashMap<>();
    ids.forEach(id -> result.put(id, new EngagementDto(likeCounts.getOrDefault(id, 0L), favoriteCounts.getOrDefault(id, 0L), likedIds.contains(id), favoredIds.contains(id))));
    return result;
  }
}

