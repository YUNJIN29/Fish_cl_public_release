package com.fishmart.upload;

import com.fishmart.config.AppProperties;
import com.fishmart.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUploadControllerTest {
  @TempDir Path tempDir;

  @AfterEach
  void clearSynchronization() {
    if (TransactionSynchronizationManager.isSynchronizationActive()) TransactionSynchronizationManager.clearSynchronization();
  }

  @Test
  void deletesMetadataAndPhysicalFileOnlyAfterTransactionCommit() throws Exception {
    UploadResourceRepository resources = mock(UploadResourceRepository.class);
    AppProperties props = new AppProperties(); props.getPrivateUpload().setDir(tempDir.toString());
    UploadResource item = new UploadResource(); item.setId("resource-id"); item.setStoredName("stored.png");
    when(resources.findById("resource-id")).thenReturn(Optional.of(item));
    Path file = tempDir.resolve("stored.png"); Files.write(file, new byte[] {1, 2, 3});
    TransactionSynchronizationManager.initSynchronization();
    var controller = new AdminUploadController(resources, mock(UserRepository.class), props);
    controller.delete("resource-id");
    assertFalse(Files.exists(file));
    assertTrue(Files.exists(tempDir.resolve(".trash/stored.png")));
    verify(resources).delete(item); verify(resources).flush();
    TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
    assertFalse(Files.exists(tempDir.resolve(".trash/stored.png")));
  }

  @Test
  void restoresPhysicalFileWhenDatabaseTransactionRollsBack() throws Exception {
    UploadResourceRepository resources = mock(UploadResourceRepository.class);
    AppProperties props = new AppProperties(); props.getPrivateUpload().setDir(tempDir.toString());
    UploadResource item = new UploadResource(); item.setId("resource-id"); item.setStoredName("stored.png");
    when(resources.findById("resource-id")).thenReturn(Optional.of(item));
    Path file = tempDir.resolve("stored.png"); Files.write(file, new byte[] {1});
    TransactionSynchronizationManager.initSynchronization();
    new AdminUploadController(resources, mock(UserRepository.class), props).delete("resource-id");
    TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
    assertTrue(Files.exists(file));
  }
}
