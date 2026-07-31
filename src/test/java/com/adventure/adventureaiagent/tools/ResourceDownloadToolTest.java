package com.adventure.adventureaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ResourceDownloadToolTest {

    @Test
    void downloadResource() {
        ResourceDownloadTool tool = new ResourceDownloadTool();
        String result = tool.downloadResource("https://qiniuyun.code-nav.cn/img/image-20211127235325557.png", "aa.png");
        Assertions.assertNotNull(result);
    }
}