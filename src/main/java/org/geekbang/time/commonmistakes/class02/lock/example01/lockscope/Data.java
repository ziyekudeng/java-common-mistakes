package org.geekbang.time.commonmistakes.class02.lock.example01.lockscope;

import lombok.Getter;

class Data {
    /**
     * @className: Data
     * @description 静态字段属于类，类级别的锁才能保护；而非静态字段属于类实例，实例级别的锁就可以保护。
     * @author gao wei
     * @date 2022/2/14/0014 17:34
     */
    @Getter
    private static int counter = 0;
    private static Object locker = new Object();

    public static int reset() {
        counter = 0;
        return counter;
    }

    public synchronized void wrong() {
        counter++;
    }

    public void right() {
        /*
         * 作用： 提升锁的级别，从实力级别提升到类级别
         */
        synchronized (locker) {
            counter++;
        }
    }
}
