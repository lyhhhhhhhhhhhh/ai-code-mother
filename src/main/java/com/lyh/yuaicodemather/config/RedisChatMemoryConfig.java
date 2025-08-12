package com.lyh.yuaicodemather.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyuhang
 * @version 1.0
 * @time 2025-08-06-13:41
 **/

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryConfig {

    private String host;
    private int port;
    private String password;
    private long ttl;

    /**
     * redis 持久化对话记忆
     * @return
     */
    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        return RedisChatMemoryStore.builder()
                .host(host)
                .port(port)
                .user("default")
                .password(password)
                .ttl(ttl)
                .build();
    }

}
