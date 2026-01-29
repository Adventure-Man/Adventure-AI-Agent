package com.adventure.adventureaiagent.demo;

import com.adventure.adventureaiagent.tools.FileOperationTool;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class FileOperationToolTest {

    @Test
    public void testReadFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "文件工具.txt";
        
        String result = tool.readFile(fileName);
        assertNotNull(result);
    }

    @Test
    public void testWriteFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "文件工具.txt";
        String content = "测试工具调用---》文件内容";
        String result = tool.writeFile(fileName, content);
        assertNotNull(result);
    }
}
