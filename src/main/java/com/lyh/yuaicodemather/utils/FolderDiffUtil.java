package com.lyh.yuaicodemather.utils;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.lyh.yuaicodemather.model.entity.DiffResultVO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FolderDiffUtil {

    /**
     * 比较两个文件夹的差异
     *
     * @param oldVersionPath 旧版本路径
     * @param newVersionPath 新版本路径
     * @return 差异结果
     * @throws IOException IO 异常
     */
    public static DiffResultVO compareFolders(String oldVersionPath, String newVersionPath) throws IOException {

        File oldDir = new File(oldVersionPath);
        File newDir = new File(newVersionPath);

        File[] oldFiles = oldDir.listFiles();
        File[] newFiles = newDir.listFiles();

        if (oldFiles == null || newFiles == null) {
            throw new IllegalArgumentException("文件夹无效");
        }

        Map<String, String> diffContentMap = new HashMap<>();
        int diffCount = 0;
        boolean allSame = true;

        // 文件名 -> 新旧文件对象
        Map<String, File> oldFileMap = Arrays.stream(oldFiles)
                .filter(File::isFile)
                .collect(Collectors.toMap(File::getName, f -> f));

        for (File newFile : newFiles) {
            if (!newFile.isFile()) continue;

            File oldFile = oldFileMap.get(newFile.getName());
            if (oldFile == null) continue; // 忽略新增文件

            List<String> oldLines = Files.readAllLines(oldFile.toPath());
            List<String> newLines = Files.readAllLines(newFile.toPath());

            Patch<String> patch = DiffUtils.diff(oldLines, newLines);

            if (!patch.getDeltas().isEmpty()) {
                allSame = false;
                diffCount++;
                String diffText = UnifiedDiffUtils.generateUnifiedDiff(
                        oldFile.getName(), newFile.getName(), oldLines, patch, 0
                ).stream().collect(Collectors.joining("\n"));
                diffContentMap.put(newFile.getName(), diffText);
            }
        }
        return new DiffResultVO(!allSame, diffCount, diffContentMap);
    }
}