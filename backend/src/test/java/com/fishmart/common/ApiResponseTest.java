package com.fishmart.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {
  @Test
  void successResponseHasStableCode() {
    ApiResponse<String> response = ApiResponse.ok("value");
    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getCode()).isEqualTo("OK");
    assertThat(response.getData()).isEqualTo("value");
  }

  @Test
  void errorResponseHasExplicitCode() {
    ApiResponse<Object> response = ApiResponse.error("RATE_LIMITED", "请求过于频繁");
    assertThat(response.isSuccess()).isFalse();
    assertThat(response.getCode()).isEqualTo("RATE_LIMITED");
    assertThat(response.getData()).isNull();
  }
}
