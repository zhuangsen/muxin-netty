package com.zs.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @auther: madison
 * @date: 2018-10-04 17:50
 * @description:
 */
@RestController
public class HelloController {
    @GetMapping("hello")
    public String hello(){
        return "hello muxin~~";
    }
}
