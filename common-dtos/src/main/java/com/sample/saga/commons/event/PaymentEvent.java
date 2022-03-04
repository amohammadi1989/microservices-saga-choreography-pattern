package com.sample.saga.commons.event;

import com.sample.saga.commons.dto.PaymentRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class PaymentEvent implements Event{
    
    private UUID eventId=UUID.randomUUID();
    private Date eventDate=new Date();
    private Integer step;
    private String tranId;
    private PaymentRequestDto paymentRequestDto;
    private PaymentStatus paymentStatus;
    private boolean inventoryStatus=false;
    private boolean customerStatus=false;
    
    @Override
    public UUID getEventId() {
        return eventId;
    }
    
    @Override
    public Date getDate() {
        return eventDate;
    }
    
    public PaymentEvent(PaymentRequestDto paymentRequestDto, PaymentStatus paymentStatus) {
        this.paymentRequestDto = paymentRequestDto;
        this.paymentStatus = paymentStatus;
    }
    
    @Override
    public String toString() {
        return "PaymentEvent{" +
        "status=" + paymentStatus +
        ",dto=" + paymentRequestDto +
        '}';
    }
    
    public boolean isInventoryStatus() {
        return inventoryStatus;
    }
    
    public boolean isCustomerStatus() {
        return customerStatus;
    }
}
