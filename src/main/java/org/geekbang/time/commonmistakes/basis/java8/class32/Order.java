package org.geekbang.time.commonmistakes.basis.java8.class32;

import lombok.Data;
import org.geekbang.time.commonmistakes.basis.java8.Customer;
import org.geekbang.time.commonmistakes.basis.java8.class31.example02.Product;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toList;

/**
 * @className: Order
 * @description 订单类
 * @author gao wei
 * @date 2022/2/15/0015 17:51
 */
@Data
public class Order {

    private Long id;
    private Long customerId;//顾客ID
    private String customerName;//顾客姓名
    private List<OrderItem> orderItemList;//订单商品明细
    private Double totalPrice;//总价格
    private LocalDateTime placedAt;//下单时间

    public static List<Order> getData() {
        List<Product> products = Product.getData();
        List<Customer> customers = Customer.getData();

        Random random = new Random();
        return LongStream.rangeClosed(1, 10).mapToObj(i ->
        {
            Order order = new Order();
            order.setId(i);
            order.setPlacedAt(LocalDateTime.now().minusHours(random.nextInt(24 * 365)));

            order.setOrderItemList(IntStream.rangeClosed(1, random.ints(1, 1, 8).findFirst().getAsInt()).mapToObj(j ->
            {
                OrderItem orderItem = new OrderItem();
                Product product = products.get(random.nextInt(products.size()));
                orderItem.setProductId(product.getId());
                orderItem.setProductName(product.getName());
                orderItem.setProductPrice(product.getPrice());
                orderItem.setProductQuantity(random.ints(1, 1, 5).findFirst().getAsInt());
                return orderItem;
            }).collect(toList()));
            order.setTotalPrice(order.getOrderItemList().stream().mapToDouble(item -> item.getProductPrice() * item.getProductQuantity()).sum());

            Customer customer = customers.get(random.nextInt(customers.size()));
            order.setCustomerId(customer.getId());
            order.setCustomerName(customer.getName());

            return order;
        }).collect(toList());
    }
}
