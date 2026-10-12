package com.fishmart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
  private final AppProperties props;

  public WebMvcConfig(AppProperties props) {
    this.props = props;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path printDir = Path.of(props.getPrint().getDir()).toAbsolutePath().normalize();
    registry.addResourceHandler("/print-files/**").addResourceLocations(directoryLocation(printDir));

  }

  private String directoryLocation(Path dir) {
    String location = dir.toUri().toString();
    return location.endsWith("/") ? location : location + "/";
  }
}

