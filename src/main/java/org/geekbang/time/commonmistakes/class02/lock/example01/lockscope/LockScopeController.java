package org.geekbang.time.commonmistakes.class02.lock.example01.lockscope;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.IntStream;

@RestController
@RequestMapping("lockscope")
@Slf4j
public class LockScopeController {

    /**
     * @return : java.lang.String
     * @title wrong2
     * @description 案例一  分析清线程、业务逻辑和锁三者之间的关系
     * 错误情况
     * @author gao wei
     * @date 2022/2/14/0014 17:35
     */
    @GetMapping("wrong2")
    public String wrong2() {

        Interesting interesting = new Interesting();
        new Thread(() -> interesting.add()).start();
        new Thread(() -> interesting.compare()).start();
        return "OK";
    }

    /**
     * @return : java.lang.String
     * @title right2
     * @description 案例一  分析清线程、业务逻辑和锁三者之间的关系
     * 修正情况
     * @author gao wei
     * @date 2022/2/14/0014 17:35
     */
    @GetMapping("right2")
    public String right2() {
        Interesting interesting = new Interesting();
        new Thread(() -> interesting.add()).start();
        new Thread(() -> interesting.compareRight()).start();
        return "OK";
    }

    /**
     * @param count :
     * @return : int
     * @title wrong
     * @description 案例二： 静态字段属于类，类级别的锁才能保护；而非静态字段属于类实例，实例级别的锁就可以保护。
     * @author gao wei
     * @date 2022/2/14/0014 17:35
     */
    @GetMapping("wrong")
    public int wrong(@RequestParam(value = "count", defaultValue = "1000000") int count) {
        //数据重置
        Data.reset();
        //多线程循环一定次数调用Data类不同实例的wrong方法
        IntStream.rangeClosed(1, count).parallel().forEach(i -> new Data().wrong());
        return Data.getCounter();
    }

    @GetMapping("right")
    public int right(@RequestParam(value = "count", defaultValue = "1000000") int count) {
        Data.reset();
        IntStream.rangeClosed(1, count).parallel().forEach(i -> new Data().right());
        return Data.getCounter();
    }
}
