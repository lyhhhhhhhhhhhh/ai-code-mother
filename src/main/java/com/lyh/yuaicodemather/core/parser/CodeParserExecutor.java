package com.lyh.yuaicodemather.core.parser;

import com.lyh.yuaicodemather.exception.BusinessException;
import com.lyh.yuaicodemather.exception.ErrorCode;
import com.lyh.yuaicodemather.model.enums.CodeGenTypeEnum;

/**
 * @author liyuhang
 * @version 1.0
 * @time 2025-08-04-15:02
 **/

public class CodeParserExecutor {

    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    /**
     * 执行代码解析
     *
     * @param codeContent 代码内容
     * @param codeGenTypeEnum 代码生成类型
     * @return 解析结果(HtmlCodeResult 或者 MultiFileCodeResult)
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的代码生成类型");
        };
    }
}
