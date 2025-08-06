package com.lyh.yuaicodemather.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionUtilsTest {

    @Test
    void getLatestTwoVersionDirs() {

        String[] versions = VersionUtils.getLatestTwoVersionDirs("multi_file_310259877404729344");
        if (versions != null) {
            System.out.println("最新版本: " + versions[0]);
            System.out.println("次新版本: " + versions[1]);
        } else {
            System.out.println("无法获取版本信息");
        }

    }
}