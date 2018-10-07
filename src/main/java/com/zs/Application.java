package com.zs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @auther: madison
 * @date: 2018-10-04 17:42
 * @description:
 */
@SpringBootApplication(scanBasePackages = {"com.zs", "org.n3r.idworker"})
// 扫描mybatis mapper路径
@MapperScan(basePackages="com.zs.mapper")
public class Application {

    @Bean
    public SpringUtil getSpringUtil(){
        return new SpringUtil();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
