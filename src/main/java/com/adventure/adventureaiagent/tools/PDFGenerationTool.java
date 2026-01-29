package com.adventure.adventureaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.adventure.adventureaiagent.common.constant.FileConstant;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.jsoup.Jsoup;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;
import java.io.IOException;

public class PDFGenerationTool {

    @Tool(description = "Generate PDF file with given content")
    public String generatePDF(@ToolParam(description = "Name of file to save PDF") String fileName,
                              @ToolParam(description = "Content to include in PDF") String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        try {
            // 创建pdf目录
            if (!FileUtil.exist(fileDir)) {
                FileUtil.mkdir(fileDir);
            }
            fileName = fileName + ".pdf";
            String filePath = fileDir + "/" + fileName;
            try (PdfDocument pdfDocument = new PdfDocument(new PdfWriter(filePath));
                 Document document = new Document(pdfDocument)) {
                // 使用内置的中文字体
                PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
                document.setFont(font);
                // 创建段落
                Paragraph paragraph = new Paragraph(content);
                // 添加段落
                document.add(paragraph);
                return "PDF generated successfully: " + fileName;
            }
        } catch (Exception e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }
}
