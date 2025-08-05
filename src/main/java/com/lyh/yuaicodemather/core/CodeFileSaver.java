package com.lyh.yuaicodemather.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.lyh.yuaicodemather.ai.model.HtmlCodeResult;
import com.lyh.yuaicodemather.ai.model.MultiFileCodeResult;
import com.lyh.yuaicodemather.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 文件保存
 *
 * @author liyuhang
 * @version 1.0
 * @time 2025-08-04-11:34
 **/
@Deprecated
public class CodeFileSaver {

    /**
     * 文件保存路径
     */
    private static final String FILE_ROOT_PATH = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "code_output" + File.separator;

    /**
     * 保存单个文件
     * @param htmlCodeResult 单文件内容
     * @return 新建文件路径
     */
    public static File saveHtmlFile(HtmlCodeResult htmlCodeResult) {
        String dirPath = buildFilePath(CodeGenTypeEnum.HTML.getValue());
        saveFile(dirPath, "index.html", htmlCodeResult.getHtmlCode());
        return new File(dirPath);
    }

    /**
     * 保存多个文件
     * @param multiFileCodeResult 多文件内容
     * @return 新建文件路径
     */
    public static File saveMutilHtmlFile(MultiFileCodeResult multiFileCodeResult) {
        String dirPath = buildFilePath(CodeGenTypeEnum.MULTI_FILE.getValue());
        saveFile(dirPath, "index.html", multiFileCodeResult.getHtmlCode());
        saveFile(dirPath, "index.css", multiFileCodeResult.getCssCode());
        saveFile(dirPath, "index.js", multiFileCodeResult.getJsCode());
        return new File(dirPath);
    }

    /**
     * 构建文件的唯一路径(tmp/code_output/bizType_雪花 ID)
     * @param bizType 代码生成类型
     * @return 文件路径
     */
    private static String buildFilePath(String bizType) {
        String uniqueDirPath = StrUtil.format("{}_{}",bizType,IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_ROOT_PATH + File.separator + uniqueDirPath;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 保存单个文件
     * @param dirPath 文件路径
     * @param filename 文件名称
     * @param content 文件内容
     */
    private static void saveFile(String dirPath, String filename, String content) {
        String filePath = dirPath + File.separator + filename;
        FileUtil.writeString(content, filePath, "UTF-8");

    }
}
