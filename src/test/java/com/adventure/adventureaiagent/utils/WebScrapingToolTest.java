package com.adventure.adventureaiagent.utils;

import com.adventure.adventureaiagent.tools.ResourceDownloadTool;
import com.adventure.adventureaiagent.tools.WebScrapingTool;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class WebScrapingToolTest {

    @Test
    public void testScrapeWebPage() {
        WebScrapingTool tool = new WebScrapingTool();
        String url = "http://localhost:3000/";
        String result = tool.scrapeWebPage(url);
        assertNotNull(result);
    }

    @Test
    public void testDownResource(){
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        String url = "https://adventure-1368999915.cos.ap-shanghai.myqcloud.com/public/2/2025-07-23/343444f9ae474a829949b7efaf27bf76.jpeg";
        resourceDownloadTool.downloadResource(url,"小熊.jpeg");
    }
}
