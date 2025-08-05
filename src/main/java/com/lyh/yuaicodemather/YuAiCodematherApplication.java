package com.lyh.yuaicodemather;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lyh.yuaicodemather.mapper")
public class YuAiCodematherApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuAiCodematherApplication.class, args);
    }

}
