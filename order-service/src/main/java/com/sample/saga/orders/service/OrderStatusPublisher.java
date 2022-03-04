package com.sample.saga.orders.service;

import com.sample.saga.commons.dto.OrderRequestDto;
import com.sample.saga.commons.event.OrderEvent;
import com.sample.saga.commons.event.OrderStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.util.Random;

@Service
public class OrderStatusPublisher {

    private final Sinks.Many<OrderEvent> orderSinks;
    
    public OrderStatusPublisher(Sinks.Many<OrderEvent> orderSinks) {
        this.orderSinks = orderSinks;
    }
    
    public void publishOrderEvent(OrderRequestDto orderRequestDto, OrderStatus orderStatus){
        OrderEvent orderEvent=new OrderEvent( orderRequestDto, orderStatus, 0,
                                              String.valueOf((new Random()).nextInt(200)) );
        orderSinks.tryEmitNext(orderEvent);
    }
}
