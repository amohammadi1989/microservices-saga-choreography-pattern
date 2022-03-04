package com.sample.saga.payment.entity;

import com.sample.saga.commons.event.CustomerStatus;
import com.sample.saga.commons.event.InventoryStatus;
import com.sample.saga.commons.event.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserTransaction {
    @Id
    private Integer orderId;
    private int userId;
    private int amount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    @Enumerated(EnumType.STRING)
    private CustomerStatus customerStatus;
    @Enumerated(EnumType.STRING)
    private InventoryStatus inventoryStatus;
}
