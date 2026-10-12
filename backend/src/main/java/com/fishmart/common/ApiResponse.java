package com.fishmart.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiResponse<T> {
  private boolean success;
  private String code;
  private String message;
  private T data;

  public ApiResponse(boolean success, String message, T data) {
    this(success, success ? "OK" : "ERROR", message, data);
  }

  public ApiResponse(boolean success, String code, String message, T data) {
    this.success = success;
    this.code = code;
    this.message = message;
    this.data = data;
  }

  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(true, "OK", "OK", data);
  }

  public static <T> ApiResponse<T> ok(String message, T data) {
    return new ApiResponse<>(true, "OK", message, data);
  }

  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>(false, "ERROR", message, null);
  }

  public static <T> ApiResponse<T> error(String code, String message) {
    return new ApiResponse<>(false, code, message, null);
  }
}

