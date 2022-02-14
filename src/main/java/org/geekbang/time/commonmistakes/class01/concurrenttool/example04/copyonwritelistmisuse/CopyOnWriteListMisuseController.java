package org.geekbang.time.commonmistakes.class01.concurrenttool.example04.copyonwritelistmisuse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author gao wei
 * @className: CopyOnWriteListMisuseController
 * @description 没有认清并发工具的使用场景，因而导致性能问题  CopyOnWriteArrayList
 * @date 2022/2/14/0014 16:37
 */
@RestController
@RequestMapping("copyonwritelistmisuse")
@Slf4j
public class CopyOnWriteListMisuseController {
    /**
     * @return : java.util.Map
     * @title testWrite
     * @description 测试copyOnWriteArrayList 并发读、写的性能
     * 结论：
     * 1.大量写的场景（10 万次 add 操作），CopyOnWriteArray 几乎比同步的 ArrayList 慢一百倍：
     * 2.大量读的场景下（100 万次 get 操作），CopyOnWriteArray 又比同步的 ArrayList 快五倍以上
     * @author gao wei
     * @date 2022/2/14/0014 16:43
     */
    @GetMapping("write")
    public Map testWrite() {

        List<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        // 此处使用为加锁集合
        List<Integer> synchronizedList = Collections.synchronizedList(new ArrayList<>());
        StopWatch stopWatch = new StopWatch();
        int loopCount = 100000;
        stopWatch.start("Write:copyOnWriteArrayList");
        //循环100000次并发往CopyOnWriteArrayList写入随机元素
        IntStream.rangeClosed(1, loopCount).parallel().forEach(__ -> copyOnWriteArrayList.add(ThreadLocalRandom.current().nextInt(loopCount)));
        stopWatch.stop();
        stopWatch.start("Write:synchronizedList");
        //循环100000次并发往加锁的ArrayList写入随机元素
        IntStream.rangeClosed(1, loopCount).parallel().forEach(__ -> synchronizedList.add(ThreadLocalRandom.current().nextInt(loopCount)));
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());
        Map result = new HashMap();
        result.put("copyOnWriteArrayList", copyOnWriteArrayList.size());
        result.put("synchronizedList", synchronizedList.size());
        return result;
    }

    /**
     * 帮助方法： 用来填充List
     *
     * @param list
     */
    private void addAll(List<Integer> list) {
        list.addAll(IntStream.rangeClosed(1, 1000000).boxed().collect(Collectors.toList()));
    }

    /**
     * @return : java.util.Map
     * @title testRead
     * @description 测试并发读的性能
     * @author gao wei
     * @date 2022/2/14/0014 16:46
     */
    @GetMapping("read")
    public Map testRead() {
        //创建两个测试对象
        List<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        List<Integer> synchronizedList = Collections.synchronizedList(new ArrayList<>());
        //填充数据
        addAll(copyOnWriteArrayList);
        addAll(synchronizedList);
        StopWatch stopWatch = new StopWatch();
        int loopCount = 1000000;
        int count = copyOnWriteArrayList.size();
        stopWatch.start("Read:copyOnWriteArrayList");
        //循环1000000次并发从CopyOnWriteArrayList随机查询元素
        IntStream.rangeClosed(1, loopCount).parallel().forEach(__ -> copyOnWriteArrayList.get(ThreadLocalRandom.current().nextInt(count)));
        stopWatch.stop();
        stopWatch.start("Read:synchronizedList");
        //循环1000000次并发从加锁的ArrayList随机查询元素
        IntStream.range(0, loopCount).parallel().forEach(__ -> synchronizedList.get(ThreadLocalRandom.current().nextInt(count)));
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());
        Map result = new HashMap();
        result.put("copyOnWriteArrayList", copyOnWriteArrayList.size());
        result.put("synchronizedList", synchronizedList.size());
        return result;
    }
}
