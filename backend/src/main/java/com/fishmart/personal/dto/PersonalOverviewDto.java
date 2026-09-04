package com.fishmart.personal.dto;

import com.fishmart.content.dto.PostDto;
import com.fishmart.diagnose.dto.DiagnoseRecordDto;
import com.fishmart.order.dto.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PersonalOverviewDto {
  private int profileCompletion;
  private Stats stats;
  private List<DiagnoseRecordDto> recentDiagnoses;
  private List<OrderDto> recentOrders;
  private List<PostDto> recentPosts;

  @Data
  @AllArgsConstructor
  public static class Stats {
    private long diagnoseCount;
    private long orderCount;
    private long postCount;
    private long favoriteCount;
  }
}
