package com.fishmart.disease;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "diseases")
public class Disease {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 128)
  private String name;

  /** 病害类型：细菌性/病毒性/寄生虫性/营养性/真菌性/其他 */
  @Column(length = 32)
  private String diseaseType;

  @Lob
  private String description;

  /** 流行地区，逗号分隔 */
  @Column(length = 512)
  private String prevalentRegions;

  /** 危害对象，逗号分隔 */
  @Column(length = 512)
  private String affectedSpecies;

  /** 引发原因 */
  @Lob
  private String causes;

  /** 症状描述 */
  @Lob
  private String symptomsSummary;

  /** 治疗方案 */
  @Lob
  private String treatment;

  /** 预防措施 */
  @Lob
  private String prevention;

  /** 流行温度范围，如 "20~28°C" */
  @Column(length = 64)
  private String prevalentTemp;

  /** 封面图URL */
  @Column(length = 512)
  private String imageUrl;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}
