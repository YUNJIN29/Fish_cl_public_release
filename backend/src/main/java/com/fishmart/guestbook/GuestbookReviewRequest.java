package com.fishmart.guestbook;

import jakarta.validation.constraints.Size;

public record GuestbookReviewRequest(
    String status,
    @Size(max = 32, message = "昵称不能超过32字") String author,
    @Size(max = 500, message = "留言不能超过500字") String content,
    @Size(max = 500, message = "回复不能超过500字") String reply
) {}
