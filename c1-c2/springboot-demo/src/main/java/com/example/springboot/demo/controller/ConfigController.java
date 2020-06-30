package com.example.springboot.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jang
 *
 * 读取配置
 */
@RequestMapping("/config")
@RestController
public class ConfigController {

    @Value("${server.port}")
    private String port;

    @Autowired
    private Environment env;

    @Autowired
    private User user;

    @GetMapping("/config")
    public Object config() {
        Map<String, Object> result = new HashMap<>();
        result.put("port", port);
        result.put("profile", env.getProperty("spring.profiles.active"));
        result.put("user", user.getName());
        return result;
    }


}
