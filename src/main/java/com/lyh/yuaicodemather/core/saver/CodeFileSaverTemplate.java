package com.lyh.yuaicodemather.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lyh.yuaicodemather.constant.AppConstant;
import com.lyh.yuaicodemather.exception.BusinessException;
import com.lyh.yuaicodemather.exception.ErrorCode;
import com.lyh.yuaicodemather.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象代码文件保存器 - 模板方法模式
 *
 * @author liyuhang
 */
public abstract class CodeFileSaverTemplate<T> {

    // 文件保存根目录
    protected static final String FILE_SAVE_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 模板方法：保存代码的标准流程
     *
     * @param result 代码结果对象
     * @param appId 应用ID
     * @return 保存的目录
     */
    public final File saveCode(T result,Long appId) {
        // 1. 验证输入
        validateInput(result);
        // 2. 构建唯一目录
        String baseDirPath = buildUniqueDir(appId);
        // 3. 创建版本目录
        String versionDir = createVersionDir(baseDirPath);
        // 4. 保存文件（具体实现由子类提供）
        saveFiles(result, versionDir);
        // 5. 返回目录文件对象
        return new File(versionDir);
    }

    /**
     * 验证输入参数（可由子类覆盖）
     *
     * @param result 代码结果对象
     */
    protected void validateInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码结果对象不能为空");
        }
    }

    /**
     * 构建唯一目录路径
     * @param appId 应用ID
     * @return 目录路径
     */
    protected final String buildUniqueDir(Long appId) {
        String codeType = getCodeType().getValue();
        if (appId == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用ID不能为空");
        }
        String uniqueDirName = StrUtil.format("{}_{}", codeType, appId);
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 创建版本目录
     * @param baseDirPath 基础目录路径
     * @return 版本目录路径
     */
    private String createVersionDir(String baseDirPath) {
        File baseDir = new File(baseDirPath);
        if (!baseDir.exists()) {
            FileUtil.mkdir(baseDir);
        }

        // 找出所有 version_x 文件夹
        int maxVersion = 0;
        for (File file : baseDir.listFiles()) {
            if (file.isDirectory() && file.getName().startsWith("version_")) {
                String versionStr = file.getName().replace("version_", "");
                try {
                    int version = Integer.parseInt(versionStr);
                    maxVersion = Math.max(maxVersion, version);
                } catch (NumberFormatException ignored) {}
            }
        }

        // 新版本号 +1
        int newVersion = maxVersion + 1;
        String versionDirName = "version_" + newVersion;
        String versionDirPath = baseDirPath + File.separator + versionDirName;

        // 创建新目录
        FileUtil.mkdir(versionDirPath);

        // 更新 version.json
        updateVersionJson(baseDirPath, newVersion, versionDirName);

        return versionDirPath;
    }

    /**
     * 创建(更新) version.json 文件
     * @param baseDirPath 基础目录路径
     * @param version 版本号
     * @param versionDirName 版本目录名称
     */
    private void updateVersionJson(String baseDirPath, int version, String versionDirName) {
        String jsonPath = baseDirPath + File.separator + "version.json";
        JSONArray versionArray;

        // 若存在则读取已有内容
        if (FileUtil.exist(jsonPath)) {
            String json = FileUtil.readString(jsonPath, StandardCharsets.UTF_8);
            versionArray = JSONUtil.parseArray(json);
        } else {
            versionArray = new JSONArray();
        }

        JSONObject versionInfo = new JSONObject();
        versionInfo.set("version", version);
        versionInfo.set("dir", versionDirName);
        versionInfo.set("timestamp", System.currentTimeMillis());

        versionArray.add(versionInfo);

        // 写入 json 文件
        FileUtil.writeUtf8String(versionArray.toStringPretty(), jsonPath);
    }

    /**
     * 写入单个文件的工具方法
     *
     * @param dirPath  目录路径
     * @param filename 文件名
     * @param content  文件内容
     */
    protected final void writeToFile(String dirPath, String filename, String content) {
        if (StrUtil.isNotBlank(content)) {
            String filePath = dirPath + File.separator + filename;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }

    /**
     * 获取代码类型（由子类实现）
     *
     * @return 代码生成类型
     */
    protected abstract CodeGenTypeEnum getCodeType();

    /**
     * 保存文件的具体实现（由子类实现）
     *
     * @param result      代码结果对象
     * @param baseDirPath 基础目录路径
     */
    protected abstract void saveFiles(T result, String baseDirPath);
}
