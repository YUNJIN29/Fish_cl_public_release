package com.fishmart.operations;

import com.fishmart.config.AppProperties;
import com.fishmart.diagnose.DiagnoseRecord;
import com.fishmart.diagnose.DiagnoseRecordRepository;
import com.fishmart.visit.DailyVisitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DataRetentionService {
  private static final Logger log = LoggerFactory.getLogger(DataRetentionService.class);
  private final AppProperties props;
  private final DailyVisitRepository visits;
  private final DiagnoseRecordRepository diagnoses;

  public DataRetentionService(AppProperties props, DailyVisitRepository visits, DiagnoseRecordRepository diagnoses) {
    this.props = props;
    this.visits = visits;
    this.diagnoses = diagnoses;
  }

  @Scheduled(cron = "${app.retention.cron:0 30 3 * * *}")
  @Transactional
  public void cleanExpiredData() {
    LocalDate visitCutoff = LocalDate.now().minusDays(props.getRetention().getVisitsDays());
    long removedVisits = visits.deleteByVisitDateBefore(visitCutoff);
    Instant diagnoseCutoff = Instant.now().minus(props.getRetention().getDiagnosesDays(), ChronoUnit.DAYS);
    List<DiagnoseRecord> expired = diagnoses.findByCreatedAtBefore(diagnoseCutoff);
    expired.forEach(record -> deleteOwnedImage(record.getImageUrl()));
    diagnoses.deleteAllInBatch(expired);
    cleanDirectory(Path.of(props.getPrint().getDir()), props.getRetention().getPrintFilesDays());
    log.info("Retention cleanup complete visits={} diagnoses={}", removedVisits, expired.size());
  }

  private void deleteOwnedImage(String imageUrl) {
    if (imageUrl == null || !imageUrl.startsWith("/diagnose-images/")) return;
    Path root = Path.of(props.getDiagnoseImage().getDir()).toAbsolutePath().normalize();
    Path target = root.resolve(imageUrl.substring("/diagnose-images/".length())).normalize();
    if (target.startsWith(root)) try { Files.deleteIfExists(target); } catch (Exception e) { log.warn("Could not remove expired diagnose image", e); }
  }

  private void cleanDirectory(Path configured, int days) {
    Path root = configured.toAbsolutePath().normalize();
    if (!Files.isDirectory(root)) return;
    Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
    try (var files = Files.list(root)) {
      files.filter(Files::isRegularFile).filter(path -> {
        try { return Files.getLastModifiedTime(path).toInstant().isBefore(cutoff); }
        catch (Exception ignored) { return false; }
      }).forEach(path -> { try { Files.deleteIfExists(path); } catch (Exception e) { log.warn("Could not remove expired print file", e); } });
    } catch (Exception e) { log.warn("Retention scan failed for {}", root, e); }
  }
}
