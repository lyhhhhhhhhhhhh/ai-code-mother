package com.lyh.yuaicodemather.core;

import com.lyh.yuaicodemather.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;


@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个登录页面", CodeGenTypeEnum.MULTI_FILE, 123123123L);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("生成一个登录页面", CodeGenTypeEnum.MULTI_FILE,1232131L);
        // 阻塞等待所有结果
        List<String> resultList = codeStream.collectList().block();
        Assertions.assertNotNull(resultList);
    }
}