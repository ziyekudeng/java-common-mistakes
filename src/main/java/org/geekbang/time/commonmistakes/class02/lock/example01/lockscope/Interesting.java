package org.geekbang.time.commonmistakes.class02.lock.example01.lockscope;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;

@Slf4j
public class Interesting {
    /**
     * @className: Interesting
     * @description 因为两个线程是交错执行 add 和 compare 方法中的业务逻辑，
     * 正确的做法应该是，为 add 和 compare 都加上方法锁，确保 add 方法执行时，compare 无法读取 a 和 b：
     * @author gao wei
     * @date 2022/2/14/0014 17:26
     */
    volatile int a = 1;
    volatile int b = 1;

    public synchronized void add() {
        log.info("add start");
        for (int i = 0; i < 1000000; i++) {
            a++;
            b++;
        }
        log.info("add done");
    }

    /*
     * 错误情况
     */
    public void compare() {
        log.info("compare start");
        for (int i = 0; i < 1000000; i++) {
            //a始终等于b吗？
            if (a < b) {
                log.info("a:{},b:{},{}", a, b, a > b);
                //最后的a>b应该始终是false的吗？
            }
        }
        log.info("compare done");
    }

    /*
     *正确做法
     */
    public synchronized void compareRight() {
        log.info("compare start");
        for (int i = 0; i < 1000000; i++) {
            Assert.assertTrue(a == b);
            if (a < b) {
                log.info("a:{},b:{},{}", a, b, a > b);
            }
        }
        log.info("compare done");
    }
}
