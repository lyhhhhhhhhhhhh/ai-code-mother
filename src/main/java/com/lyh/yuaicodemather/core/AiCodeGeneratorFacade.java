package com.lyh.yuaicodemather.core;

import com.lyh.yuaicodemather.ai.AiCodeGeneratorService;
import com.lyh.yuaicodemather.ai.AiCodeGeneratorServiceFactory;
import com.lyh.yuaicodemather.ai.model.HtmlCodeResult;
import com.lyh.yuaicodemather.ai.model.MultiFileCodeResult;
import com.lyh.yuaicodemather.core.parser.CodeParserExecutor;
import com.lyh.yuaicodemather.core.saver.CodeFileSaverExecutor;
import com.lyh.yuaicodemather.exception.BusinessException;
import com.lyh.yuaicodemather.exception.ErrorCode;
import com.lyh.yuaicodemather.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * 代码生成门面类,组合代码生成和保存功能
 * @author liyuhang
 * @version 1.0
 * @time 2025-08-04-13:20
 **/
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;



    /**
     * 统一入口，根据需求生成并且保存代码
     * @param userMessage 用户提示词
     * @param appId 应用id
     * @param codeGenTypeEnum 创建文件的类型
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        if (userMessage == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数不能为空");
        }
        // 根据appId 获取相应的AI服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum){
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield  CodeFileSaverExecutor.executeSaver(htmlCodeResult,CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield  CodeFileSaverExecutor.executeSaver(multiFileCodeResult,CodeGenTypeEnum.MULTI_FILE,appId);
            }
            default -> {
                String errorMessage = "不支持的代码生成类型" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR,errorMessage);
            }
        };
    }

    /**
     * 统一入口，根据需求生成并且保存代码(流式)
     * @param userMessage 用户提示词
     * @param appId 应用id
     * @param codeGenTypeEnum 创建文件的类型
     * yield 是 Java 14 引入的新语法 新的 switch 表达式语法（Java 14+） 中，使用 yield 可以从 case 块中返回值
     * yield  processCodeStream(codeStream,codeGenTypeEnum,appId); 这一步可以理解为 return codeStream
     * 直接将流返回出去了 而processCodeStream其实是对流的一个"副作用"对流做额外的操作 而不改变流的本质
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        if (userMessage == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数不能为空");
        }
        // 根据appId 获取相应的AI服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum){
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield  processCodeStream(codeStream,codeGenTypeEnum,appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream,codeGenTypeEnum,appId);
            }
            default -> {
                String errorMessage = "不支持的代码生成类型" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR,errorMessage);
            }
        };
    }

    /**
     * 生成多个文件并保存(流式)
     * @param codeStream 代码流
     * @param codeGenType 代码生成类型
     * @param appId 应用id
     * @return 返回的文件路径
     */
    private Flux<String> processCodeStream(Flux<String> codeStream,CodeGenTypeEnum codeGenType,Long appId) {
        // 定义字符串拼接器 用于当流式返回全部代码之后在保存代码
        StringBuilder codeBuilder = new StringBuilder();
        // 这里return 返回的内容其实就是codeStream 所以在上一步
        return codeStream.doOnNext(chunk -> {
            // 实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 当流式返回全部代码之后在保存代码
            String completeResult = codeBuilder.toString();
            // 解析代码为对象
            Object parserObject = CodeParserExecutor.executeParser(completeResult, codeGenType);
            // 使用执行器保存代码
            File fileDir = CodeFileSaverExecutor.executeSaver(parserObject, codeGenType,appId);
            log.info("文件保存成功,文件路径为:{}",fileDir.getAbsolutePath());
        });
    }
}