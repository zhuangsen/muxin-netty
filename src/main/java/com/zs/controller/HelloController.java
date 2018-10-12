package com.zs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @auther: madison
 * @date: 2018-10-04 17:50
 * @description:
 */
@RestController
public class HelloController {

    private Logger logger = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("hello")

    public String hello(){

        logger.info("hello-info");
        logger.debug("hello-debug");
        logger.warn("hello-warn");
        logger.error("hello-error");

        return "hello muxin~~";
    }
}
