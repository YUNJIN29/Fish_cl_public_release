package com.fishmart.guestbook;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuestbookRequest(
    @NotBlank(message = "昵称不能为空")
    @Size(max = 32, message = "昵称不能超过32字")
    String author,
    @NotBlank(message = "留言内容不能为空")
    @Size(max = 500, message = "留言不能超过500字")
    String content
) {}
