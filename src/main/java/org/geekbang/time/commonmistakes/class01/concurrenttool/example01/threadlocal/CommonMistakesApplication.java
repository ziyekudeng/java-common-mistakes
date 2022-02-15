package org.geekbang.time.commonmistakes.class01.concurrenttool.example01.threadlocal;

import lombok.extern.slf4j.Slf4j;
import org.geekbang.time.commonmistakes.common.Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@Slf4j
public class CommonMistakesApplication {

    public static void main(String[] args) {
        // 测试查看ThreadLocal 重用问题
        Utils.loadPropertySource(CommonMistakesApplication.class, "tomcat.properties");

        SpringApplication.run(CommonMistakesApplication.class, args);

        log.info("----------------------------------------");
        log.info("class01 example01 启动成功");
        log.info("----------------------------------------");
    }
}

