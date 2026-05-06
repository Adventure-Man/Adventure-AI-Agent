package com.adventure.adventureaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.adventure.adventureaiagent.common.constant.FileConstant;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;

@Slf4j
public class PDFGenerationTool {

    // 自定义字体格式
    private final String fontPath;

    // 兜底格式- 微软雅黑
    private final String fallbackFont;
    // 字体编码格式
    private final String fontEncoding;

    public PDFGenerationTool(
            String fontPath,
             String fallbackFont,
             String fontEncoding) {
        this.fontPath = fontPath;
        this.fallbackFont = fallbackFont;
        this.fontEncoding = fontEncoding;
    }

    @Tool(description = "Generate PDF file with given content")
    public String generatePDF(@ToolParam(description = "Name of the PDF file to save (.pdf extension will be added automatically if not provided)") String fileName,
                              @ToolParam(description = "Text content to include in the PDF, supports Chinese characters") String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        try {
            // 创建pdf目录
            if (!FileUtil.exist(fileDir)) {
                FileUtil.mkdir(fileDir);
            }
            // 确保文件名有.pdf扩展名
            if (!fileName.toLowerCase().endsWith(".pdf")) {
                fileName = fileName + ".pdf";
            }
//            fileName = fileName + ".pdf";
            String filePath = fileDir + "/" + fileName;
            try (PdfDocument pdfDocument = new PdfDocument(new PdfWriter(filePath));
                 Document document = new Document(pdfDocument)) {
                // 使用内置的中文字体
//                PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
                // 设置微软雅黑字体
//                PdfFont font = PdfFontFactory.createFont( "C:/Windows/Fonts/MiSans-Regular.otf", "Identity-H");
                PdfFont fontWithFallback = createFontWithFallback();
                document.setFont(fontWithFallback);
                // 创建段落
                Paragraph paragraph = new Paragraph(content);
                // 添加段落
                document.add(paragraph);
                return "PDF generated successfully: " + fileName;
            }
        } catch (Exception e) {
            log.info("Error generating PDF: " + e.getMessage());
            return "Error generating PDF: " + e.getMessage();
        }
    }

    /**
     * 创建字体，支持降级方案
     */
    private PdfFont createFontWithFallback() throws IOException {
        try {
            // 尝试使用配置的字体
            if (FileUtil.exist(fontPath)) {
                log.debug("Using configured font: {}", fontPath);
                return PdfFontFactory.createFont(fontPath, fontEncoding);
            } else {
                log.warn("Configured font not found: {}, using fallback font", fontPath);
            }
        } catch (Exception e) {
            log.warn("Failed to load configured font: {}, using fallback font. Error: {}", fontPath, e.getMessage());
        }
        // 使用降级字体（iText内置字体）
        log.debug("Using fallback font: {}", fallbackFont);
        return PdfFontFactory.createFont(fallbackFont, "UniGB-UCS2-H");
    }
}
