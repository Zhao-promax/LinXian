package com.cirida.zhao.linxian.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cirida.zhao.linxian.common.exception.BizException;
import com.cirida.zhao.linxian.common.result.ErrorCode;
import com.cirida.zhao.linxian.common.util.JwtUtil;
import com.cirida.zhao.linxian.user.dto.LoginRequest;
import com.cirida.zhao.linxian.user.dto.RegisterRequest;
import com.cirida.zhao.linxian.user.dto.UserVO;
import com.cirida.zhao.linxian.user.entity.User;
import com.cirida.zhao.linxian.user.mapper.UserMapper;
import com.cirida.zhao.linxian.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户注册/登录，密码 BCrypt 加密，登录签发 JWT。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.ttl}")
    private long jwtTtl;

    @Override
    public UserVO register(RegisterRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count != null && count > 0) {
            throw new BizException(ErrorCode.USER_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname());
        user.setStatus(1);
        userMapper.insert(user);
        return toVO(user);
    }

    @Override
    public UserVO login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(ErrorCode.LOGIN_FAILED);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }

        UserVO vo = toVO(user);
        vo.setToken(JwtUtil.createToken(jwtSecret, jwtTtl, user.getId(), user.getUsername()));
        return vo;
    }

    @Override
    public UserVO getById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        return toVO(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setStatus(user.getStatus());
        return vo;
    }
}
