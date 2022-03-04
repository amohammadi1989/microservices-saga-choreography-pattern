package com.sample.saga.commons.event;

import com.sample.saga.commons.dto.OrderRequestDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.UUID;
@NoArgsConstructor
@Data
@SuperBuilder
public class OrderEvent implements Event{

    private UUID eventId=UUID.randomUUID();
    private Integer step=0;
    private String tranId;
    private Date eventDate=new Date();
    private OrderRequestDto orderRequestDto;
    private OrderStatus orderStatus;

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Date getDate() {
        return eventDate;
    }

    public OrderEvent(OrderRequestDto orderRequestDto, OrderStatus orderStatus,Integer step,
                      String tranId) {
        this.orderRequestDto = orderRequestDto;
        this.orderStatus = orderStatus;
        this.step=step;
        this.tranId=tranId;
    }
    
    @Override
    public String toString() {
        return "OrderEvent{" +
        "status=" + orderStatus +
        ",dto=" + orderRequestDto +
        '}';
    }
}
