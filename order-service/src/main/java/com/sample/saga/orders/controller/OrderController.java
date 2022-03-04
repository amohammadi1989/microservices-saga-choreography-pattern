package com.sample.saga.orders.controller;

import com.sample.saga.commons.dto.OrderRequestDto;
import com.sample.saga.commons.event.OrderStatus;
import com.sample.saga.orders.entity.PurchaseOrder;
import com.sample.saga.orders.repository.OrderRepository;
import com.sample.saga.orders.service.OrderStatusPublisher;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {
    
    private final OrderRepository orderRepository;
    private final OrderStatusPublisher orderStatusPublisher;
    
    public OrderController(OrderStatusPublisher orderStatusPublisher, OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.orderStatusPublisher = orderStatusPublisher;
    }
    
    @PostMapping("/create")
    public PurchaseOrder createOrder(@RequestBody OrderRequestDto orderRequestDto){
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setProductId(orderRequestDto.getProductId());
        purchaseOrder.setUserId(orderRequestDto.getUserId());
        purchaseOrder.setOrderStatus( OrderStatus.ORDER_CREATED);
        purchaseOrder.setPrice(orderRequestDto.getAmount());
        PurchaseOrder order=orderRepository.save( purchaseOrder );
        orderRequestDto.setOrderId( order.getId() );
        orderStatusPublisher.publishOrderEvent(  orderRequestDto,OrderStatus.ORDER_CREATED);
        return order;
    }


}
