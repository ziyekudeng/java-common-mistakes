package org.geekbang.time.commonmistakes.class02.lock.example02.lockgranularity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@Slf4j
public class CommonMistakesApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonMistakesApplication.class, args);
        log.info("----------------------------------------");
        log.info("class02 example02 启动成功");
        log.info("----------------------------------------");
    }
}
