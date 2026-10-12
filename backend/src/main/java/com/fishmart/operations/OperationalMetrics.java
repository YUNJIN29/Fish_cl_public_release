package com.fishmart.operations;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

@Component
public class OperationalMetrics {
  private final LongAdder loginSuccess = new LongAdder();
  private final LongAdder loginFailure = new LongAdder();
  private final LongAdder diagnoseSuccess = new LongAdder();
  private final LongAdder diagnoseFailure = new LongAdder();
  private final LongAdder diagnoseDurationMs = new LongAdder();
  private final LongAdder uploadFailure = new LongAdder();
  private final LongAdder rateLimited = new LongAdder();

  public void login(boolean success) { (success ? loginSuccess : loginFailure).increment(); }
  public void diagnose(boolean success, long durationMs) {
    (success ? diagnoseSuccess : diagnoseFailure).increment();
    diagnoseDurationMs.add(Math.max(durationMs, 0));
  }
  public void uploadFailure() { uploadFailure.increment(); }
  public void rateLimited() { rateLimited.increment(); }

  public Map<String, Object> snapshot() {
    long successful = diagnoseSuccess.sum();
    long failed = diagnoseFailure.sum();
    long calls = successful + failed;
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("loginSuccess", loginSuccess.sum());
    result.put("loginFailure", loginFailure.sum());
    result.put("diagnoseSuccess", successful);
    result.put("diagnoseFailure", failed);
    result.put("diagnoseAverageMs", calls == 0 ? 0 : diagnoseDurationMs.sum() / calls);
    result.put("uploadFailure", uploadFailure.sum());
    result.put("rateLimited", rateLimited.sum());
    return result;
  }
}
