package com.adventure.adventureaiagent.controller;

import com.adventure.adventureaiagent.common.annotation.RateLimit;
import com.adventure.adventureaiagent.common.resp.BaseResponse;
import com.adventure.adventureaiagent.common.utils.IpHelperUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 主控制器
 */
@RestController
public class MainController {
//    @Autowired
//    private FileUploadService fileUploadService;

//    @PostMapping("/upload")
//    public String upload(@RequestParam("file") MultipartFile file) {
//        return fileUploadService.uploadFile(file);
//    }
    /**
     * 健康检查
     *
     * @return 响应
     */
    @RateLimit(key = "love_app_sse", minuteLimit = 5, dayLimit = 20, monthLimit = 100)
    @GetMapping("/health")
    public String healthCheck() {
        return "Hello World!";
    }

    /**
     * 登录测试
     *
     * @return 响应
     */
    @PostMapping("/login")
    public BaseResponse<Map<String, Object>> healthCheckTwo(@RequestBody LoginRequest loginRequest) {
        System.out.println(loginRequest.rememberMe());
        LoginRequest admin = new LoginRequest("admin", "123456", false);
        System.out.println(admin.userAccount()); // Changed from getRememberMe() to getUserAccount()
        System.out.println(admin.userPassword());
        System.out.println(admin.rememberMe());
        Map<String, Object> userMap = null;
        userMap = new HashMap<>(Map.of());
        userMap.put("userId", 1);
        userMap.put("userAccount", "admin");
        userMap.put("userPassword", "123456");

        userMap.put("ip",IpHelperUtils.getIpAddr());
        return new BaseResponse<>(userMap);
    }


    /**
     * 登录测试
     *
     * @return 响应
     */
    @PostMapping("/login/test")
    public BaseResponse<Map<String, Object>> healthCheckThree(@RequestParam String userAccount,
                                                              @RequestParam String userPassword,
                                                              @RequestParam(required = false) Boolean rememberMe) {
        System.out.println(userAccount);
        System.out.println(userPassword);
        System.out.println(rememberMe);
        Map<String, Object> userMap = new HashMap<>(Map.of());
        userMap.put("userId", 1);
        userMap.put("userAccount", "dd");
        userMap.put("userPassword", userPassword);
        userMap.put("rememberMe", rememberMe);
        return new BaseResponse<>(userMap);
    }

    public record LoginRequest(String userAccount, String userPassword, Boolean rememberMe) {

    }
}
