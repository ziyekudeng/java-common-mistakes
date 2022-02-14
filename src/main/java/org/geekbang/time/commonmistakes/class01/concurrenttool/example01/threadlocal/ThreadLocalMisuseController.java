package org.geekbang.time.commonmistakes.class01.concurrenttool.example01.threadlocal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
/**
 * @className: ThreadLocalMisuseController
 * @description 没有意识到线程重用导致用户信息错乱的 Bug
 * @author gao wei
 * @date 2022/2/14/0014 10:46
 */
@RestController
@RequestMapping("threadlocal")
public class ThreadLocalMisuseController {

    private static final ThreadLocal<Integer> currentUser = ThreadLocal.withInitial(() -> null);

    @GetMapping("wrong")
    public Map wrong(@RequestParam("userId") Integer userId) {
        String before  = Thread.currentThread().getName() + ":" + currentUser.get();
        currentUser.set(userId);
        String after  = Thread.currentThread().getName() + ":" + currentUser.get();
        Map result = new HashMap();
        result.put("before", before);
        result.put("after", after);
        return result;
    }

    @GetMapping("right")
    public Map right(@RequestParam("userId") Integer userId) {
        String before  = Thread.currentThread().getName() + ":" + currentUser.get();
        currentUser.set(userId);
        try {
            String after = Thread.currentThread().getName() + ":" + currentUser.get();
            Map result = new HashMap();
            result.put("before", before);
            result.put("after", after);
            return result;
        } finally {
            //在finally代码块中删除ThreadLocal中的数据，确保数据不串
            currentUser.remove();
        }
    }
}
