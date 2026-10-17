package com.cirida.zhao.linxian.gateway.filter;

import com.cirida.zhao.linxian.common.constant.Constants;
import com.cirida.zhao.linxian.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * 网关 JWT 全局过滤器：白名单放行，其余请求校验 Bearer Token，
 * 校验通过后把用户身份透传到下游（X-User-Id / X-Username）。
 */
@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    /** 无需鉴权的路径（注册 / 登录） */
    private static final Set<String> WHITELIST = Set.of("/user/register", "/user/login");

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (WHITELIST.contains(path)) {
            return chain.filter(exchange);
        }

        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        try {
            Claims claims = JwtUtil.parseToken(jwtSecret, auth.substring("Bearer ".length()));
            Object userIdObj = claims.get(Constants.JWT_CLAIM_USER_ID);
            String username = claims.getSubject();
            if (userIdObj == null) {
                return unauthorized(exchange);
            }

            Long userId = Long.valueOf(userIdObj.toString());
            ServerHttpRequest.Builder builder = exchange.getRequest().mutate()
                    .header(Constants.HEADER_USER_ID, String.valueOf(userId));
            if (username != null) {
                builder.header(Constants.HEADER_USERNAME, username);
            }
            ServerHttpRequest mutated = builder.build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
