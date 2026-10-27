package com.fishmart.guestbook;

import com.fishmart.content.ContentStatus;
import com.fishmart.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminGuestbookControllerTest {
  @Mock GuestbookRepository repository;

  @Test void administratorCanEditAndReply() {
    User user = new User(); user.setNickname("访客");
    GuestbookMessage item = new GuestbookMessage(); item.setId(1L); item.setUser(user); item.setAuthorName("匿名用户");
    item.setContent("原留言"); item.setStatus(ContentStatus.APPROVED);
    when(repository.findById(1L)).thenReturn(Optional.of(item));
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var result = new AdminGuestbookController(repository).review(1L,
        new GuestbookReviewRequest(null, null, "修改后的留言", "感谢留言")).getData();

    assertThat(result.content()).isEqualTo("修改后的留言");
    assertThat(result.reply()).isEqualTo("感谢留言");
  }

  @Test void administratorCanDelete() {
    when(repository.existsById(2L)).thenReturn(true);
    new AdminGuestbookController(repository).delete(2L);
    verify(repository).deleteById(2L);
  }
}
