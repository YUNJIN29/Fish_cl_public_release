package com.fishmart.diagnose;

import com.fishmart.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "diagnose_records")
public class DiagnoseRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(columnDefinition = "TEXT")
  private String description; // 用户症状描述

  @Column(columnDefinition = "TEXT")
  private String result; // AI 诊断结果

  private String species; // 鱼种
  private String temperature; // 水温
  private String waterQuality; // 水质
  private String duration; // 发病时长
  private String imageUrl; // 病灶图片

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}
