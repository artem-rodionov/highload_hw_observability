package org.example.observability_java.Service;

import lombok.AllArgsConstructor;
import org.example.observability_java.Entity.Order;
import org.example.observability_java.Repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository repository;

    public Order GetOrder(long orderID) {
        return repository.findById(orderID).orElseThrow(
                () -> new NoSuchElementException(String.valueOf(orderID))
        );
    }

    public long CreateOrder(long userID, long price) {
        Order order = new Order();
        order.setUserId(userID);
        order.setPrice(price);
        Order createdOrder = repository.save(order);
        return createdOrder.getId();
    }

    public Order UpdateOrder(long orderID, long userID, long price) {
        Order order1 = repository.findById(orderID).orElseThrow(
                () -> new NoSuchElementException(String.valueOf(orderID))
        );
        order1.setPrice(price);
        order1.setUserId(userID);
        return repository.save(order1);
    }
}
