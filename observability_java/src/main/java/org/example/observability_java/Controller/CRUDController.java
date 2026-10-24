package org.example.observability_java.Controller;

import lombok.AllArgsConstructor;
import org.example.observability_java.DTO.GetOrderResponse;
import org.example.observability_java.DTO.HealthResponse;
import org.example.observability_java.DTO.OrderCreateRequest;
import org.example.observability_java.DTO.OrderCreateResponse;
import org.example.observability_java.DTO.OrderUpdateRequest;
import org.example.observability_java.DTO.OrderUpdateResponse;
import org.example.observability_java.Entity.Order;
import org.example.observability_java.Metrics.TrackGoldenMetrics;
import org.example.observability_java.Service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class CRUDController {
    private final OrderService service;

    @TrackGoldenMetrics
    @GetMapping("/get/order/{id}")
    public GetOrderResponse GetObject(@PathVariable("id") Long id) {
        Order order = service.GetOrder(id);
        return new GetOrderResponse(order.getId(), order.getUserId(), order.getPrice(), order.getUpdatedAt());
    }

    @TrackGoldenMetrics
    @PostMapping("/post/order")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderCreateResponse PostObject(@RequestBody OrderCreateRequest request) {
        Long id = service.CreateOrder(request.userID(), request.price());
        return new OrderCreateResponse(id);
    }

    @TrackGoldenMetrics
    @PutMapping("/put/order/{id}")
    public OrderUpdateResponse DeleteObject(@PathVariable("id") Long id, @RequestBody OrderUpdateRequest request) {

        Order order = service.UpdateOrder(id, request.userID(), request.price());
        return new OrderUpdateResponse(order.getId(), order.getUserId(), order.getPrice(), order.getUpdatedAt());
    }
}
