package org.geekbang.time.commonmistakes.class01.concurrenttool.example03.concurrenthashmapperformance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author gao wei
 * @className: ConcurrentHashMapPerformanceController
 * @description 没有充分了解并发工具的特性，从而无法发挥其威力 ConcurrentHashMap
 * @date 2022/2/14/0014 16:15
 */
@RestController
@RequestMapping("concurrenthashmapperformance")
@Slf4j
public class ConcurrentHashMapPerformanceController {

    //循环次数
    private static int LOOP_COUNT = 10000000;
    //线程数量
    private static int THREAD_COUNT = 10;
    //元素数量
    private static int ITEM_COUNT = 10;

    /**
     * @return : java.lang.String
     * @title 性能一般，无法充分发挥 ConcurrentHashMap 的威力
     * @description
     * @author gao wei
     * @date 2022/2/14/0014 16:10
     */
    private Map<String, Long> normaluse() throws InterruptedException {
        ConcurrentHashMap<String, Long> concurrentHashMap = new ConcurrentHashMap<>(ITEM_COUNT);
        ForkJoinPool forkJoinPool = new ForkJoinPool(THREAD_COUNT);
        forkJoinPool.execute(() -> IntStream.rangeClosed(1, LOOP_COUNT).parallel().forEach(i -> {
                    //获得一个随机的Key
                    String key = "item" + ThreadLocalRandom.current().nextInt(ITEM_COUNT);
                    synchronized (concurrentHashMap) {
                        if (concurrentHashMap.containsKey(key)) {
                            //Key存在则+1
                            concurrentHashMap.put(key, concurrentHashMap.get(key) + 1);
                        } else {
                            //Key不存在则初始化为1
                            concurrentHashMap.put(key, 1L);
                        }
                    }
                }
        ));
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.HOURS);
        return concurrentHashMap;
    }

    /**
     * @return : java.util.Map<java.lang.String,java.lang.Long>
     * @title gooduse
     * @description 性能改进方法
     * @author gao wei
     * @date 2022/2/14/0014 16:17
     */

    private Map<String, Long> gooduse() throws InterruptedException {
        ConcurrentHashMap<String, LongAdder> concurrentHashMap = new ConcurrentHashMap<>(ITEM_COUNT);
        ForkJoinPool forkJoinPool = new ForkJoinPool(THREAD_COUNT);
        forkJoinPool.execute(() -> IntStream.rangeClosed(1, LOOP_COUNT).parallel().forEach(i -> {
                    String key = "item" + ThreadLocalRandom.current().nextInt(ITEM_COUNT);
                    /*
                     * 1.computeIfAbsent 如果 key 对应的 value 不存在，则使用获取 remappingFunction 重新计算后的值，
                     * 并保存为该 key 的 value，否则返回 value。
                     * 2.LongAdder 是一个多线程高并发时使用的自增计数器
                     */
                    //利用computeIfAbsent()方法来实例化LongAdder，然后利用LongAdder来进行线程安全计数
                    concurrentHashMap.computeIfAbsent(key, k -> new LongAdder()).increment();
                }
        ));
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.HOURS);
        //因为我们的Value是LongAdder而不是Long，所以需要做一次转换才能返回
        return concurrentHashMap.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().longValue())
                );
    }

    @GetMapping("good")
    public String good() throws InterruptedException {
        /*
         *StopWatch是位于org.springframework.util包下的一个工具类，通过它可方便的对程序部分代码进行计时(ms级别)，适用于同步单线程代码块。
         */

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("normaluse");
        Map<String, Long> normaluse = normaluse();
        stopWatch.stop();
        //校验元素数量
        Assert.isTrue(normaluse.size() == ITEM_COUNT, "normaluse size error");
        //校验累计总数
        Assert.isTrue(normaluse.entrySet().stream()
                        .mapToLong(item -> item.getValue()).reduce(0, Long::sum) == LOOP_COUNT
                , "normaluse count error");
        stopWatch.start("gooduse");
        Map<String, Long> gooduse = gooduse();
        stopWatch.stop();
        Assert.isTrue(gooduse.size() == ITEM_COUNT, "gooduse size error");
        Assert.isTrue(gooduse.entrySet().stream()
                        .mapToLong(item -> item.getValue())
                        .reduce(0, Long::sum) == LOOP_COUNT
                , "gooduse count error");
        log.info(stopWatch.prettyPrint());
        return "OK";
    }

}
