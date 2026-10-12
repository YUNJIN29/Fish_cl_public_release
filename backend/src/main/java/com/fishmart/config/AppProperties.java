package com.fishmart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
  private Jwt jwt = new Jwt();
  private Upload upload = new Upload();
  private PrivateUpload privateUpload = new PrivateUpload();
  private Print print = new Print();
  private DiagnoseImage diagnoseImage = new DiagnoseImage();
  private Retention retention = new Retention();
  private Cors cors = new Cors();
  private Ai ai = new Ai();

  @Data
  public static class Jwt {
    private String issuer;
    private String secret;
    private long accessTokenMinutes = 240;
  }

  @Data
  public static class Upload {
    private String dir = "uploads";
  }

  @Data
  public static class PrivateUpload {
    private String dir = "private-uploads";
  }

  @Data
  public static class Print {
    private String dir = "print-files";
  }

  @Data
  public static class DiagnoseImage {
    private String dir = "diagnose-images";
  }

  @Data
  public static class Retention {
    private int visitsDays = 90;
    private int diagnosesDays = 90;
    private int printFilesDays = 7;
  }

  @Data
  public static class Cors {
    private List<String> allowedOrigins = new ArrayList<>();
  }

  @Data
  public static class Ai {
    private String glm4BaseUrl = "";
    private String apiKey = "";
    private String model = "qwen3:8b";
  }
}

