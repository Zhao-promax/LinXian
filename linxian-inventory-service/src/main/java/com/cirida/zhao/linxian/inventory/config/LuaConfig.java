package com.cirida.zhao.linxian.inventory.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * Redis Lua 脚本配置。
 */
@Configuration
public class LuaConfig {

    @Bean
    public DefaultRedisScript<Long> preDeductScript(@Value("classpath:lua/pre_deduct.lua") Resource script) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptSource(new ResourceScriptSource(script));
        return redisScript;
    }
}
