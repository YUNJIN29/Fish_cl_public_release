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
          User u = userRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
          FavoriteEntity fe = new FavoriteEntity();
          fe.setUser(u);
          fe.setTargetType(type);
          fe.setTargetId(targetId);
          favRepo.save(fe);
          return true;
        });
  }
}

