package com.adventure.adventureaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.adventure.adventureaiagent.common.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;


public class ResourceDownloadTool {

    private static final int TIMEOUT_MS = 30_000;
    private static final long MAX_BYTES = 20L * 1024 * 1024;

    @Tool(description = "从指定 URL 下载资源并保存到本地。仅当用户明确要求下载某 URL 资源时调用；禁止搜索/抓取后自动下载。")
    public String downloadResource(@ToolParam(description = "要下载的资源 URL") String url,
                                   @ToolParam(description = "保存用的文件名（不含路径）") String fileName) {
        String safeName = sanitizeFileName(fileName);
        if (safeName == null) {
            return "下载失败：文件名无效，请提供不含路径的合法文件名";
        }
        if (StrUtil.isBlank(url)) {
            return "下载失败：URL 不能为空";
        }

        String fileDir = FileConstant.FILE_SAVE_DIR + "/download";
        String filePath = fileDir + "/" + safeName;
        File destFile = new File(filePath);
        try {
            if (!FileUtil.exist(fileDir)) {
                FileUtil.mkdir(fileDir);
            }

            try (HttpResponse response = HttpRequest.get(url)
                    .timeout(TIMEOUT_MS)
                    .execute()) {
                if (!response.isOk()) {
                    return "下载失败：HTTP " + response.getStatus();
                }
                long contentLength = response.contentLength();
                if (contentLength > MAX_BYTES) {
                    return "下载失败：资源超过 20MB 上限";
                }
                response.writeBody(destFile);
            }

            long size = FileUtil.size(destFile);
            if (size > MAX_BYTES) {
                FileUtil.del(destFile);
                return "下载失败：资源超过 20MB 上限";
            }
            return "资源已下载：" + safeName + "（路径：" + filePath + "）";
        } catch (Exception e) {
            if (FileUtil.exist(destFile)) {
                FileUtil.del(destFile);
            }
            return "下载失败：" + e.getMessage();
        }
    }

    /**
     * 取 basename，去掉路径分隔符与 {@code ..}，防止写出 tmp/download 目录。
     */
    private String sanitizeFileName(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return null;
        }
        String name = fileName.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.replace("..", "").trim();
        if (name.isEmpty() || name.contains("/") || name.contains("\\")) {
            return null;
        }
        return name;
    }
}
