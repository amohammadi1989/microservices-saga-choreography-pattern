package com.sample.saga.orders.repository;

import com.sample.saga.orders.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<PurchaseOrder,Integer> {

}
