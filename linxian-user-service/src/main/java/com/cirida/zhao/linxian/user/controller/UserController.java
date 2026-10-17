package com.cirida.zhao.linxian.user.controller;

import com.cirida.zhao.linxian.common.constant.Constants;
import com.cirida.zhao.linxian.common.exception.BizException;
import com.cirida.zhao.linxian.common.result.ErrorCode;
import com.cirida.zhao.linxian.common.result.Result;
import com.cirida.zhao.linxian.user.dto.LoginRequest;
import com.cirida.zhao.linxian.user.dto.RegisterRequest;
import com.cirida.zhao.linxian.user.dto.UserVO;
import com.cirida.zhao.linxian.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口。网关路由 {@code /user/**}，本服务映射保持相同前缀。
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(userService.register(request));
    }

    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(userService.login(request));
    }

    @GetMapping("/me")
    public Result<UserVO> me(@RequestHeader(value = Constants.HEADER_USER_ID, required = false) Long userId) {
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return Result.ok(userService.getById(userId));
    }
}
