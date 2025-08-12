package com.lyh.yuaicodemather;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.lyh.yuaicodemather.mapper")
public class YuAiCodematherApplication {
    public static void main(String[] args) {
        SpringApplication.run(YuAiCodematherApplication.class, args);
    }
}
