package com.fishmart.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
  @NotBlank
  @Size(max = 128)
  private String currentPassword;

  @NotBlank
  @Size(min = 10, max = 128, message = "新密码长度应为 10 至 128 位")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "新密码必须同时包含字母和数字")
  private String newPassword;
}
