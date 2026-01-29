package com.adventure.adventureaiagent.utils;

import com.adventure.adventureaiagent.tools.PDFGenerationTool;

import java.io.IOException;

public class HelloPdfApp {

    public static void main(String[] args) throws IOException {
//      try (Document document = new Document(new PdfDocument(new PdfWriter("./hello-pdf.pdf")))) {
//            document.add(new Paragraph("Hello PDF!"));
//        }
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        pdfGenerationTool.generatePDF("hello-pdf", "Hello PDF!");
    }
}