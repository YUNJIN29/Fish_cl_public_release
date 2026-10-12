package com.fishmart.upload;

import com.fishmart.common.BizException;
import com.fishmart.config.AppProperties;
import com.fishmart.security.AuthUser;
import com.fishmart.user.RoleName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UploadControllerTest {
  @TempDir Path tempDir;

  @Test
  void storesValidatedFilesBehindAnOpaqueResourceUrl() throws Exception {
    AppProperties props = new AppProperties();
    props.getPrivateUpload().setDir(tempDir.toString());
    UploadResourceRepository repository = mock(UploadResourceRepository.class);
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    UploadController controller = new UploadController(props, repository);
    byte[] png = new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1, 2, 3};
    var response = controller.upload(new MockMultipartFile("file", "pond.png", "image/png", png), new AuthUser(7L, "owner", Set.of(RoleName.USER)));
    assertTrue(response.isSuccess());
    assertTrue(response.getData().get("url").toString().startsWith("/api/resources/"));
    try (var files = Files.list(tempDir)) { assertEquals(1, files.count()); }
    verify(repository).save(argThat(resource -> resource.getOwnerUserId().equals(7L) && resource.getOriginalName().equals("pond.png")));
  }

  @Test
  void rejectsAnExecutableDisguisedAsAnImage() {
    AppProperties props = new AppProperties();
    props.getPrivateUpload().setDir(tempDir.toString());
    UploadResourceRepository repository = mock(UploadResourceRepository.class);
    UploadController controller = new UploadController(props, repository);
    var fake = new MockMultipartFile("file", "attack.png", "image/png", "not an image".getBytes());
    assertThrows(BizException.class, () -> controller.upload(fake, new AuthUser(7L, "owner", Set.of(RoleName.USER))));
    verify(repository, never()).save(any());
  }
}
