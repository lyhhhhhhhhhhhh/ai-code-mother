package com.lyh.yuaicodemather.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lyh.yuaicodemather.constant.AppConstant;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class VersionUtils {

    /**
     * 获取指定 deployKey 下最新版本的目录名（如 "version_2"）
     *
     * @param deployKey 部署键
     * @return 最新版本目录名，失败时返回 null
     */
    public static String getLatestVersionDir(String deployKey) {
        try {
            String versionJsonPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/" + deployKey + "/version.json";
            File jsonFile = new File(versionJsonPath);
            if (!jsonFile.exists()) {
                return null;
            }

            // 读取 JSON 内容
            String jsonStr = Files.readString(jsonFile.toPath(), StandardCharsets.UTF_8);
            JSONArray jsonArray = JSONUtil.parseArray(jsonStr);

            if (jsonArray.isEmpty()) {
                return null;
            }

            JSONObject latestObj = null;
            long maxTimestamp = Long.MIN_VALUE;

            for (Object obj : jsonArray) {
                JSONObject json = (JSONObject) obj;
                long timestamp = json.getLong("timestamp", 0L);
                if (timestamp > maxTimestamp) {
                    maxTimestamp = timestamp;
                    latestObj = json;
                }
            }

            return latestObj != null ? latestObj.getStr("dir") : null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取指定 deployKey 下最近的两个版本目录名（最新的和次新的）
     *
     * @param deployKey 部署键
     * @return 包含最近两个版本目录名的数组，[0]是最新版本，[1]是次新版本。
     * 如果没有足够版本则返回null或部分为null的数组
     */
    public static String[] getLatestTwoVersionDirs(String deployKey) {
        try {
            String versionJsonPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/" + deployKey + "/version.json";
            File jsonFile = new File(versionJsonPath);
            if (!jsonFile.exists()) {
                return null;
            }

            // 读取 JSON 内容
            String jsonStr = Files.readString(jsonFile.toPath(), StandardCharsets.UTF_8);
            JSONArray jsonArray = JSONUtil.parseArray(jsonStr);

            if (jsonArray.isEmpty()) {
                return null;
            }

            // 如果只有一个版本
            if (jsonArray.size() == 1) {
                JSONObject latestObj = jsonArray.getJSONObject(0);
                return new String[]{latestObj.getStr("dir"), null};
            }

            // 对版本按时间戳降序排序
            jsonArray.sort((a, b) -> {
                JSONObject objA = (JSONObject) a;
                JSONObject objB = (JSONObject) b;
                return Long.compare(objB.getLong("timestamp", 0L), objA.getLong("timestamp", 0L));
            });

            // 获取前两个版本
            JSONObject latest = jsonArray.getJSONObject(0);
            JSONObject secondLatest = jsonArray.getJSONObject(1);

            return new String[]{latest.getStr("dir"), secondLatest.getStr("dir")};

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}