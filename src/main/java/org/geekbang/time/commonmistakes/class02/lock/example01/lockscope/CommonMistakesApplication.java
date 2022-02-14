package org.geekbang.time.commonmistakes.class02.lock.example01.lockscope;

import lombok.extern.java.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Log
public class CommonMistakesApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonMistakesApplication.class, args);
        log.info("----------------------------------------");
        log.info("class02 example01 启动成功");
        log.info("----------------------------------------");
    }
}

