package com.jungmini.pay.common.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;


@Configuration
public class LocalRedisConfig {

    private RedisServer redisServer;

    public LocalRedisConfig(@Value("${spring.data.redis.port}") int port) {
        this.redisServer = new RedisServer(port);
    }

    @PostConstruct
    public void startRedis() {
        this.redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        this.redisServer.stop();
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        return Redisson.create(config);
    }
}
