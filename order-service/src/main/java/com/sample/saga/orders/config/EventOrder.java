package com.sample.saga.orders.config;

import com.sample.saga.commons.dto.OrderRequestDto;
import com.sample.saga.commons.event.OrderEvent;
import com.sample.saga.commons.event.OrderStatus;
import com.sample.saga.commons.event.PaymentEvent;

import com.sample.saga.commons.event.PaymentStatus;
import com.sample.saga.orders.entity.PurchaseOrder;
import com.sample.saga.orders.log.LogService;
import com.sample.saga.orders.repository.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.function.Function;

@Configuration
public class EventOrder {
    
    private final LogService logService;
    private final OrderRepository orderRepository;
    
    public EventOrder(LogService logService, OrderRepository orderRepository) {
        this.logService = logService;
        this.orderRepository = orderRepository;
    }
    
    @Bean
    public Function<Flux<PaymentEvent>, Flux<OrderEvent>> paymentEventConsumer(){
        
        return paymentEventFlux -> paymentEventFlux.flatMap( this::paymentProcess );
    }
    
    private Mono<OrderEvent> paymentProcess(PaymentEvent paymentEvent){
        
        log( paymentEvent );
        
        if(paymentEvent.getPaymentStatus().equals( PaymentStatus.PAYMENT_CANCELED )) {
            saveStateOfPayment( paymentEvent );
            return getOrderEvent( paymentEvent );
        }
        if(paymentEvent.getPaymentStatus().equals( PaymentStatus.PAYMENT_COMPLETED )
        ) {
            return getFinish( paymentEvent );
        }
        return null;
    }
    
    private Mono<OrderEvent> getFinish(PaymentEvent paymentEvent) {
        return Mono.fromRunnable( () -> {
            saveStateOfPayment( paymentEvent );
            logService.saveLog( (paymentEvent.getStep() + 2), paymentEvent.getTranId(),
                                "finish" );
        } );
    }
    
    private void log(PaymentEvent paymentEvent) {
        String log=MessageFormat.format( "{0}", paymentEvent.toString() );
        logService.saveLog( paymentEvent.getStep()+1, paymentEvent.getTranId(), log );
    }
    
    private void saveStateOfPayment(PaymentEvent paymentEvent) {
        PurchaseOrder po=
        orderRepository.findById( paymentEvent.getPaymentRequestDto().getOrderId() ).orElse( null );
        if(po!=null) {
            po.setPaymentStatus( paymentEvent.getPaymentStatus() );
            po.setOrderStatus( paymentEvent.getPaymentStatus().equals( PaymentStatus.PAYMENT_COMPLETED ) ? OrderStatus.ORDER_COMPLETED : OrderStatus.ORDER_CANCELLED );
            orderRepository.save( po );
        }
    }
    
    
    private Mono<OrderEvent> getOrderEvent(PaymentEvent paymentEvent) {
        OrderRequestDto orderRequestDto=
        OrderRequestDto.builder().orderId( paymentEvent.getPaymentRequestDto().getOrderId() )
        .amount( paymentEvent.getPaymentRequestDto().getAmount() )
        .productId( paymentEvent.getPaymentRequestDto().getProductId() )
        .userId( paymentEvent.getPaymentRequestDto().getUserId() ).build();
        
        OrderEvent orderEvent=OrderEvent.builder().orderStatus( OrderStatus.ORDER_CANCELLED )
        .orderRequestDto( orderRequestDto )
        .tranId( paymentEvent.getTranId() )
        .step( paymentEvent.getStep()+1 )
        .build();
        return Mono.fromSupplier(()-> orderEvent );
    }
    
}

