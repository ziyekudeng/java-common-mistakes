package org.geekbang.time.commonmistakes.class02.lock.example02.lockgranularity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@RestController
@RequestMapping("lockgranularity")
@Slf4j
public class LockGranularityController {
    /**
     * @className: LockGranularityController
     * @description 加锁要考虑锁的粒度和场景问题
     * 细粒度锁使用方式建议:
     * 对于读写比例差异明显的场景，考虑使用 ReentrantReadWriteLock 细化区分读写锁，来提高性能。
     * 如果你的 JDK 版本高于 1.8、共享资源的冲突概率也没那么大的话，考虑使用 StampedLock 的乐观读的特性，进一步提高性能。
     * JDK 里 ReentrantLock 和 ReentrantReadWriteLock 都提供了公平锁的版本，
     * 在没有明确需求的情况下不要轻易开启公平锁特性，在任务很轻的情况下开启公平锁可能会让性能下降上百倍。
     * @author gao wei
     * @date 2022/2/15/0015 9:53
     */
    private List<Integer> data = new ArrayList<>();

    //不涉及共享资源的慢方法
    private void slow() {
        try {
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
        }
    }

    /**
     * @return : int
     * @title wrong
     * @description 错误的加锁方法
     * @author gao wei
     * @date 2022/2/15/0015 9:55
     */
    @GetMapping("wrong")
    public int wrong() {

        long begin = System.currentTimeMillis();
        IntStream.rangeClosed(1, 1000).parallel().forEach(i -> {
            //加锁粒度太粗了
            synchronized (this) {
                slow();
                data.add(i);
            }
        });
        log.info("took:{}", System.currentTimeMillis() - begin);
        return data.size();
    }

    /**
     * @return : int
     * @title right
     * @description 正确的加锁方法
     * @author gao wei
     * @date 2022/2/15/0015 9:56
     */
    @GetMapping("right")
    public int right() {

        long begin = System.currentTimeMillis();
        IntStream.rangeClosed(1, 1000).parallel().forEach(i -> {
            slow();
            //只对List加锁
            synchronized (data) {
                data.add(i);
            }
        });
        log.info("took:{}", System.currentTimeMillis() - begin);
        return data.size();
    }

}
