package com.lyh.yuaicodemather.ai;


import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 服务创建工厂
 * @author liyuhang
 * @version 1.0
 * @time 2025-08-04-10:41
 **/

@Configuration
public class AiCodeGeneratorServiceFactory{

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    /**
     * 创建AI代码生成服务(流式)
     *
     * @return
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService(){
        return AiServices.builder(AiCodeGeneratorService.class).
                chatModel(chatModel).
                streamingChatModel(streamingChatModel)
                .build();
    }
}
