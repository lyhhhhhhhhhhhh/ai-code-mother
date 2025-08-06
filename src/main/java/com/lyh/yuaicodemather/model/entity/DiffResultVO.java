package com.lyh.yuaicodemather.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiffResultVO {
    private boolean hasDiff; // 是否存在差异
    private int diffFileCount; // 有差异的文件数量
    private Map<String, String> diffContentMap; // 文件名 -> 差异内容（文本）
}