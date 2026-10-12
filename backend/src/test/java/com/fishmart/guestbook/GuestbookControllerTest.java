package com.fishmart.guestbook;

import com.fishmart.content.ContentStatus;
import com.fishmart.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestbookControllerTest {
  @Mock GuestbookRepository repository;

  @Test void publicListOnlyRequestsApprovedMessagesAndHidesStatus() {
    User user = new User(); user.setNickname("云锦");
    GuestbookMessage item = new GuestbookMessage(); item.setId(1L); item.setUser(user); item.setContent("继续加油");
    item.setStatus(ContentStatus.APPROVED); item.setCreatedAt(Instant.parse("2026-10-12T00:00:00Z"));
    when(repository.findByStatusOrderByCreatedAtDesc(eq(ContentStatus.APPROVED), any())).thenReturn(new PageImpl<>(List.of(item)));

    item.setAuthorName("云锦");
    var page = new GuestbookController(repository).list(0, 6).getData();

    assertThat(page.getContent()).hasSize(1);
    assertThat(page.getContent().get(0).author()).isEqualTo("云锦");
    assertThat(page.getContent().get(0).status()).isNull();
  }

  @Test void anonymousMessageStartsPendingWithProvidedNickname() {
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var result = new GuestbookController(repository).create(new GuestbookRequest("  路过的人  ", "  你好，Aquatic Lab  ")).getData();

    ArgumentCaptor<GuestbookMessage> captor = ArgumentCaptor.forClass(GuestbookMessage.class);
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().getContent()).isEqualTo("你好，Aquatic Lab");
    assertThat(captor.getValue().getAuthorName()).isEqualTo("路过的人");
    assertThat(captor.getValue().getUser()).isNull();
    assertThat(captor.getValue().getStatus()).isEqualTo(ContentStatus.PENDING);
    assertThat(result.status()).isEqualTo("PENDING");
  }
}
