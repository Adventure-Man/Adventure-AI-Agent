package com.adventure.adventureaiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class WebScrapingToolTest {

    @Test
    public void testScrapeWebPage() {
        WebScrapingTool tool = new WebScrapingTool();
        String html = """
                <html>
                  <head>
                    <title>Java 学习路线</title>
                    <script>alert('x')</script>
                    <style>.a { color: red; }</style>
                  </head>
                  <body>
                    <noscript>请启用 JS</noscript>
                    <iframe src="about:blank"></iframe>
                    <svg><circle r="1"/></svg>
                    <p>这是正文内容，用于验证纯文本提取。</p>
                  </body>
                </html>
                """;
        Document doc = Jsoup.parse(html);
        String result = tool.extractReadableContent(doc);

        assertNotNull(result);
        assertFalse(result.toLowerCase().contains("<html"));
        assertFalse(result.toLowerCase().contains("<script"));
        assertTrue(result.contains("标题：Java 学习路线"));
        assertTrue(result.contains("这是正文内容"));
        assertTrue(result.length() <= WebScrapingTool.MAX_CONTENT_LENGTH + "...(已截断)".length());
    }

    @Test
    public void testExtractReadableContentTruncatesLongText() {
        WebScrapingTool tool = new WebScrapingTool();
        String longBody = "字".repeat(WebScrapingTool.MAX_CONTENT_LENGTH + 500);
        Document doc = Jsoup.parse("<html><head><title>长文</title></head><body><p>" + longBody + "</p></body></html>");
        String result = tool.extractReadableContent(doc);

        assertTrue(result.endsWith("...(已截断)"));
        assertTrue(result.length() <= WebScrapingTool.MAX_CONTENT_LENGTH + "...(已截断)".length());
        assertFalse(result.toLowerCase().contains("<html"));
        assertFalse(result.toLowerCase().contains("<script"));
    }

    @Test
    public void testDownResource() {
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        String url = "https://adventure-1368999915.cos.ap-shanghai.myqcloud.com/public/2/2025-07-23/343444f9ae474a829949b7efaf27bf76.jpeg";
        resourceDownloadTool.downloadResource(url, "小熊.jpeg");
    }
}
