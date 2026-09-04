package com.fishmart.visit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class IpLocationService {
  private final ObjectMapper mapper;
  private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
  public IpLocationService(ObjectMapper mapper) { this.mapper = mapper; }

  public String resolve(String ip) {
    if (ip == null || ip.isBlank() || isPrivate(ip)) return "未知地区";
    try {
      String fields = "status,country,regionName,city,district";
      URI uri = URI.create("http://ip-api.com/json/" + ip + "?lang=zh-CN&fields=" + fields);
      HttpRequest req = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5)).GET().build();
      JsonNode json = mapper.readTree(client.send(req, HttpResponse.BodyHandlers.ofString()).body());
      if (!"success".equals(json.path("status").asText())) return "未知地区";
      List<String> parts = new ArrayList<>();
      for (String field : List.of("country", "regionName", "city", "district")) {
        String value = json.path(field).asText("").trim();
        if (!value.isEmpty() && !parts.contains(value)) parts.add(value);
      }
      return parts.isEmpty() ? "未知地区" : String.join(" · ", parts);
    } catch (Exception ignored) { return "未知地区"; }
  }

  private boolean isPrivate(String ip) {
    return ip.equals("127.0.0.1") || ip.equals("::1") || ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("172.16.") || ip.startsWith("172.17.") || ip.startsWith("172.18.") || ip.startsWith("172.19.") || ip.startsWith("172.2");
  }
}
