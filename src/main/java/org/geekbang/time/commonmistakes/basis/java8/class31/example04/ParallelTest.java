package org.geekbang.time.commonmistakes.basis.java8.class31.example04;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
public class ParallelTest {
    /**
     * @title parallel
     * @description Stream 并行流 不确保执行顺序

     * @return : void
     * @author gao wei
     * @date 2022/2/15/0015 15:46
     */
    @Test
    public void parallel() {
        /*
         * 此处不能使用forEachOrdered，forEachOrdered会让整个遍历过程失去并行化的效能
         */
        IntStream.rangeClosed(1, 100).parallel().forEach(i -> {
            System.out.println(LocalDateTime.now() + " : " + i);
            try {
                System.out.println("----------------------------------");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * @title allMethods
     * @description 五种方式 实现多线程并行处理的方法
     * @return : void
     * @author gao wei
     * @date 2022/2/15/0015 17:35
     */
    @Test
    public void allMethods() throws InterruptedException, ExecutionException {

        int taskCount = 10000;
        int threadCount = 20;
        StopWatch stopWatch = new StopWatch();

        stopWatch.start("thread");
        Assert.assertEquals(taskCount, thread(taskCount, threadCount));
        stopWatch.stop();

        stopWatch.start("threadpool");
        Assert.assertEquals(taskCount, threadpool(taskCount, threadCount));
        stopWatch.stop();

        /*
         * 试试把这段放到forkjoin下面？
         * 建议是，设置 ForkJoinPool 公共线程池默认并行度的操作，应该放在应用启动时设置。
         */
        stopWatch.start("stream");
        Assert.assertEquals(taskCount, stream(taskCount, threadCount));
        stopWatch.stop();

        stopWatch.start("forkjoin");
        Assert.assertEquals(taskCount, forkjoin(taskCount, threadCount));
        stopWatch.stop();

        stopWatch.start("completableFuture");
        Assert.assertEquals(taskCount, completableFuture(taskCount, threadCount));
        stopWatch.stop();

        log.info(stopWatch.prettyPrint());
    }

    private void increment(AtomicInteger atomicInteger) {
        /**
         * @title increment
         * @description 场景：使用 20 个线程（threadCount）以并行方式总计执行 10000 次（taskCount）操作。
         * 因为单个任务单线程执行需要 10 毫秒（任务代码如下），也就是每秒吞吐量是 100 个操作，
         * 那 20 个线程 QPS 是 2000，执行完 10000 次操作最少耗时 5 秒。
         * @param atomicInteger :
         * @return : void
         * @author gao wei
         * @date 2022/2/15/0015 16:14
         */
        atomicInteger.incrementAndGet();
        try {
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @title thread
     * @description 多线程并行处理性能：方式一 CountDownLatch
     * @param taskCount :
     * @param threadCount :
     * @return : int
     * @author gao wei
     * @date 2022/2/15/0015 16:16
     */
    private int thread(int taskCount, int threadCount) throws InterruptedException {

        //总操作次数计数器
        AtomicInteger atomicInteger = new AtomicInteger();
        //使用CountDownLatch来等待所有线程执行完成
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        //使用IntStream把数字直接转为Thread
        IntStream.rangeClosed(1, threadCount).mapToObj(i -> new Thread(() -> {
            //手动把taskCount分成taskCount份，每一份有一个线程执行
            IntStream.rangeClosed(1, taskCount / threadCount).forEach(j -> increment(atomicInteger));
            //每一个线程处理完成自己那部分数据之后，countDown一次
            countDownLatch.countDown();
        })).forEach(Thread::start);
        //等到所有线程执行完成
        countDownLatch.await();
        //查询计数器当前值
        return atomicInteger.get();
    }

    /**
     * @title threadpool
     * @description 多线程并行处理性能：方式二 Executors.newFixedThreadPool
     * @param taskCount :
     * @param threadCount :
     * @return : int
     * @author gao wei
     * @date 2022/2/15/0015 16:58
     */

    private int threadpool(int taskCount, int threadCount) throws InterruptedException {
        //总操作次数计数器
        AtomicInteger atomicInteger = new AtomicInteger();
        //初始化一个线程数量=threadCount的线程池
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        //所有任务直接提交到线程池处理
        IntStream.rangeClosed(1, taskCount).forEach(i -> executorService.execute(() -> increment(atomicInteger)));
        //提交关闭线程池申请，等待之前所有任务执行完成
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
        //查询计数器当前值
        return atomicInteger.get();
    }

    /**
     * @title forkjoin
     * @description 多线程并行处理性能：方式三 ForkJoinPool
     * 1.ForkJoinPool 更适合大任务分割成许多小任务并行执行的场景
     * 2.ThreadPoolExecutor 适合许多独立任务并发执行的场景。
     * @param taskCount :
     * @param threadCount :
     * @return : int
     * @author gao wei
     * @date 2022/2/15/0015 16:58
     */

    private int forkjoin(int taskCount, int threadCount) throws InterruptedException {
        //总操作次数计数器
        AtomicInteger atomicInteger = new AtomicInteger();
        //自定义一个并行度=threadCount的ForkJoinPool
        ForkJoinPool forkJoinPool = new ForkJoinPool(threadCount);
        //所有任务直接提交到线程池处理
        forkJoinPool.execute(() -> IntStream.rangeClosed(1, taskCount).parallel().forEach(i -> increment(atomicInteger)));
        //提交关闭线程池申请，等待之前所有任务执行完成
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.HOURS);
        //查询计数器当前值
        return atomicInteger.get();
    }

    /**
     * @title stream
     * @description 多线程并行处理性能：方式四 直接使用并行流
     * @param taskCount :
     * @param threadCount :
     * @return : int
     * @author gao wei
     * @date 2022/2/15/0015 16:58
     */

    private int stream(int taskCount, int threadCount) {
        //设置公共ForkJoinPool的并行度
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(threadCount));
        //总操作次数计数器
        AtomicInteger atomicInteger = new AtomicInteger();
        //由于我们设置了公共ForkJoinPool的并行度，直接使用parallel提交任务即可
        IntStream.rangeClosed(1, taskCount).parallel().forEach(i -> increment(atomicInteger));
        //查询计数器当前值
        return atomicInteger.get();
    }

    /**
     * @title completableFuture
     * @description 多线程并行处理性能：方式五 CompletableFuture
     * @param taskCount :
     * @param threadCount :
     * @return : int
     * @author gao wei
     * @date 2022/2/15/0015 16:58
     */

    private int completableFuture(int taskCount, int threadCount) throws InterruptedException, ExecutionException {
        //总操作次数计数器
        AtomicInteger atomicInteger = new AtomicInteger();
        //自定义一个并行度=threadCount的ForkJoinPool
        ForkJoinPool forkJoinPool = new ForkJoinPool(threadCount);
        //使用CompletableFuture.runAsync通过指定线程池异步执行任务
        CompletableFuture.runAsync(() -> IntStream.rangeClosed(1, taskCount).parallel().forEach(i -> increment(atomicInteger)), forkJoinPool).get();
        //查询计数器当前值
        return atomicInteger.get();
    }
}
