package com.fishmart.content;

import com.fishmart.common.BizException;
import com.fishmart.content.dto.FavoriteItemDto;
import com.fishmart.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentServiceFavoriteTest {
  @Mock ArticleRepository articleRepo;
  @Mock PostRepository postRepo;
  @Mock CommentRepository commentRepo;
  @Mock LikeRepository likeRepo;
  @Mock FavoriteRepository favoriteRepo;
  @Mock UserRepository userRepo;
  private ContentService service;

  @BeforeEach
  void setUp() {
    service = new ContentService(articleRepo, postRepo, commentRepo, likeRepo, favoriteRepo, userRepo);
  }

  @Test
  void listsOnlyTheAuthenticatedUsersFavoritesWithResolvedContent() {
    FavoriteEntity favorite = new FavoriteEntity();
    favorite.setId(9L); favorite.setTargetType(TargetType.ARTICLE); favorite.setTargetId(3L); favorite.setCreatedAt(Instant.now());
    Article article = new Article();
    article.setId(3L); article.setTitle("草鱼常见病预防"); article.setContent("<p>保持水质稳定并持续观察。</p>"); article.setStatus(ContentStatus.APPROVED);
    when(favoriteRepo.findByUserIdOrderByCreatedAtDesc(eq(7L), any())).thenReturn(new PageImpl<>(List.of(favorite)));
    when(articleRepo.findById(3L)).thenReturn(Optional.of(article));

    FavoriteItemDto item = service.listFavorites(7L, null, PageRequest.of(0, 12)).getContent().get(0);
    assertEquals("草鱼常见病预防", item.getTitle());
    assertEquals("保持水质稳定并持续观察。", item.getSummary());
    assertEquals("/fish/articles/3", item.getUrl());
    assertTrue(item.isAvailable());
  }

  @Test
  void rejectsFavoritingMissingContent() {
    when(favoriteRepo.findByUserIdAndTargetTypeAndTargetId(7L, TargetType.POST, 99L)).thenReturn(Optional.empty());
    when(postRepo.findById(99L)).thenReturn(Optional.empty());
    assertThrows(BizException.class, () -> service.toggleFavorite(7L, TargetType.POST, 99L));
    verify(favoriteRepo, never()).save(any());
  }

  @Test
  void returnsPublicEngagementCountsWithoutUserState() {
    TargetCountView likes = countView(3L, 8L);
    TargetCountView favorites = countView(3L, 5L);
    when(likeRepo.countGrouped(eq(TargetType.ARTICLE), any())).thenReturn(List.of(likes));
    when(favoriteRepo.countGrouped(eq(TargetType.ARTICLE), any())).thenReturn(List.of(favorites));
    var result = service.engagement(TargetType.ARTICLE, List.of(3L), null).get(3L);
    assertEquals(8L, result.getLikeCount());
    assertEquals(5L, result.getFavoriteCount());
    assertFalse(result.isLiked());
    assertFalse(result.isFavored());
  }

  private TargetCountView countView(Long id, long total) {
    return new TargetCountView() {
      public Long getTargetId() { return id; }
      public long getTotal() { return total; }
    };
  }
}
