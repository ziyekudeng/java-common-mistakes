package org.geekbang.time.commonmistakes.basis.java8.class32;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *@program: java-common-mistakes
 *@description: 顾客类
 *@author: gao wei
 *@create: 2022-02-15 17:50
 */
@Data
@AllArgsConstructor
public class Customer {
    private Long id;
    private String name;//顾客姓名
}
