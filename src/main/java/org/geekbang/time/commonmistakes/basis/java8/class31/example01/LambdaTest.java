package org.geekbang.time.commonmistakes.basis.java8.class31.example01;

import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author gao wei
 * @className: LambdaTest
 * @description Lambda 表达式
 * @date 2022/2/15/0015 13:02
 */
public class LambdaTest {

    @Test
    public void lambdavsanonymousclass() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("hello1");
            }
        }).start();

        new Thread(() -> System.out.println("hello2")).start();
    }

    @Test
    public void functionalInterfaces() {
        /*
         *1.可以看一下java.util.function包
         */
        //使用方法引用提供Supplier接口实现，返回空字符串
        Supplier<String> supplier = String::new;
        System.out.println(supplier);
        //使用Lambda表达式提供Supplier接口实现，返回OK字符串
        Supplier<String> stringSupplier = () -> "OK";
        System.out.println(supplier);
        /*
         *2.Predicate的例子
         */

        /*
         *Predicate接口是输入一个参数，返回布尔值。我们通过and方法组合两个Predicate条件，判断是否值大于0并且是偶数
         */
        Predicate<Integer> positiveNumber = i -> i > 0;
        Predicate<Integer> evenNumber = i -> i % 2 == 0;
        assertTrue(positiveNumber.and(evenNumber).test(2));

        /*
         *Consumer接口是消费一个数据。我们通过andThen方法组合调用两个Consumer，输出两行abcdefg
         */
        Consumer<String> println = System.out::println;
        println.andThen(println).accept("abcdefg");

        /*
         *Function接口是输入一个数据，计算后输出一个数据。我们先把字符串转换为大写，然后通过andThen组合另一个Function实现字符串拼接
         */
        Function<String, String> upperCase = String::toUpperCase;
        Function<String, String> duplicate = s -> s.concat(s);
        assertThat(upperCase.andThen(duplicate).apply("test"), is("TESTTEST"));

        /*
         *Supplier是提供一个数据的接口。这里我们实现获取一个随机数
         */
        Supplier<Integer> random = () -> ThreadLocalRandom.current().nextInt();
        System.out.println(random.get());

        /*
         *BinaryOperator是输入两个同类型参数，输出一个同类型参数的接口。这里我们通过方法引用获得一个整数加法操作，通过Lambda表达式定义一个减法操作，
         * 然后依次调用
         */
        BinaryOperator<Integer> add = Integer::sum;
        BinaryOperator<Integer> subtraction = (a, b) -> a - b;
        assertThat(subtraction.apply(add.apply(1, 2), 3), is(0));
    }
}
