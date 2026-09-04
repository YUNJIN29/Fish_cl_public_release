package com.fishmart.auth.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
  @Size(min = 2, max = 32)
  private String nickname;

  @Size(max = 512)
  private String avatarUrl;

  @Size(max = 128)
  private String email;

  @Size(max = 32)
  private String phone;

  @Size(max = 128)
  private String farmName;

  @Size(max = 128)
  private String farmLocation;

  @Size(max = 128)
  private String mainSpecies;

  @Size(max = 512)
  private String bio;
}
