package com.cirida.zhao.linxian.user.service;

import com.cirida.zhao.linxian.user.dto.LoginRequest;
import com.cirida.zhao.linxian.user.dto.RegisterRequest;
import com.cirida.zhao.linxian.user.dto.UserVO;

public interface UserService {

    UserVO register(RegisterRequest request);

    UserVO login(LoginRequest request);

    UserVO getById(Long userId);
}
