package com.fishmart.admin;

import com.fishmart.admin.dto.UserDto;
import com.fishmart.common.ApiResponse;
import com.fishmart.common.BizException;
import com.fishmart.user.User;
import com.fishmart.user.UserRepository;
import com.fishmart.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
  private final UserRepository userRepo;

  public AdminUserController(UserRepository userRepo) {
    this.userRepo = userRepo;
  }

  @GetMapping
  public ApiResponse<Page<UserDto>> list(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
    PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "id"));
    return ApiResponse.ok(userRepo.findAll(pr).map(UserDto::from));
  }

  @PatchMapping("/{id}/status")
  @Transactional
  public ApiResponse<UserDto> setStatus(@PathVariable Long id, @RequestParam UserStatus status) {
    User u = userRepo.findById(id).orElseThrow(() -> new BizException("用户不存在"));
    u.setStatus(status);
    return ApiResponse.ok(UserDto.from(userRepo.save(u)));
  }
}

