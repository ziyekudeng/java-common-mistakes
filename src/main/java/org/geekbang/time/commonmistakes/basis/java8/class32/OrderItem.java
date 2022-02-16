package org.geekbang.time.commonmistakes.basis.java8.class32;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @className: OrderItem
 * @description 订单商品类
 * @author gao wei
 * @date 2022/2/15/0015 17:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private Long productId;//商品ID
    private String productName;//商品名称
    private Double productPrice;//商品价格
    private Integer productQuantity;//商品数量
}
