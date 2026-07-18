package com.adventure.adventureaiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

public class WebScrapingTool {

    public static final int MAX_CONTENT_LENGTH = 8000;

    @Tool(description = "抓取指定网页的全文内容。仅当用户明确要求打开/抓取某 URL 时调用；禁止在普通问答或搜索完成后自动抓取搜到的链接。")
    public String scrapeWebPage(@ToolParam(description = "URL of the web page to scrape") String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return extractReadableContent(doc);
        } catch (IOException e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }

    /**
     * 去噪并提取可读正文，供工具返回与单测复用。
     */
    public String extractReadableContent(Document doc) {
        doc.select("script, style, noscript, iframe, svg").remove();
        String title = doc.title() != null ? doc.title().trim() : "";
        String bodyText = doc.body() != null ? doc.body().text() : doc.text();
        bodyText = bodyText.replaceAll("\\s+", " ").trim();
        String content = "标题：" + title + "\n正文：" + bodyText;
        if (content.length() > MAX_CONTENT_LENGTH) {
            return content.substring(0, MAX_CONTENT_LENGTH) + "...(已截断)";
        }
        return content;
    }
}
