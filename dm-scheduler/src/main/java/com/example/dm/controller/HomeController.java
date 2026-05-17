package com.example.dm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "<h1>剧本杀门店与DM主持人调度管理系统</h1>" +
               "<p>后端服务已成功启动！</p>" +
               "<p><a href='http://localhost:8080'>前往前端系统</a></p>";
    }
    
    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}