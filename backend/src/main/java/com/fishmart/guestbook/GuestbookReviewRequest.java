package com.fishmart.guestbook;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuestbookReviewRequest(
    @NotBlank(message = "审核状态不能为空") String status,
    @Size(max = 500, message = "回复不能超过500字") String reply
) {}
