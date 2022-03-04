package com.sample.saga.payment.config;

import com.sample.saga.commons.dto.OrderRequestDto;
import com.sample.saga.commons.dto.PaymentRequestDto;
import com.sample.saga.commons.event.*;
import com.sample.saga.payment.entity.UserTransaction;
import com.sample.saga.payment.log.LogService;
import com.sample.saga.payment.repository.UserTransactionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Configuration
public class EventPayment {
    
    
    private final UserTransactionRepository userTransactionRepository;
    private final LogService logService;
    
    public EventPayment(UserTransactionRepository userTransactionRepository, LogService logService) {
        this.userTransactionRepository = userTransactionRepository;
        this.logService = logService;
    }
    
    @Bean
    public Function<Flux<CustomerEvent>,Flux<PaymentEvent>> paymentCustomerProcessor(){
        return paymentEventFlux -> paymentEventFlux.flatMap(this::processPayment);
    }
    
    @Bean
    public Function<Flux<InventoryEvent>,Flux<PaymentEvent>> inventoryProcessor(){
        return inventoryEventFlux -> inventoryEventFlux.flatMap( this::processInventoryPayment);
    }
    
    @Bean
    public Function<Flux<OrderEvent>, Flux<PaymentEvent>> paymentProcessor() {
        return orderEventFlux -> orderEventFlux.flatMap(this::processPayment);
        
    }
    
    private Mono<PaymentEvent> processInventoryPayment(InventoryEvent inventoryEvent){
        
        logService( inventoryEvent.toString(), inventoryEvent.getStep(), inventoryEvent.getTranId() );
        UserTransaction userTransaction = getUser( inventoryEvent.getInventoryRequestDto().getOrderId() );
        
        if(userTransaction!=null) {
            return processInventoryEvent( inventoryEvent, userTransaction );
        }
        
        return null;
    }
    
    private Mono<PaymentEvent> processInventoryEvent(InventoryEvent inventoryEvent, UserTransaction userTransaction) {
        
        if (inventoryEvent.getInventoryStatus().equals( InventoryStatus.INVENTORY_COMPLETED )) {
            userTransaction.setInventoryStatus( InventoryStatus.INVENTORY_COMPLETED );
            userTransactionRepository.save( userTransaction );
            Mono<PaymentEvent> inventoryResult = whenInventoryComplete( inventoryEvent, userTransaction );
            if (inventoryResult != null) return inventoryResult;
        }
        
        if (inventoryEvent.getInventoryStatus().equals( InventoryStatus.INVENTORY_FAILED )) {
            return whenInventoryFails( inventoryEvent, userTransaction );
        }
        return Mono.fromRunnable( () -> {
        } );
    }
    
    private Mono<PaymentEvent> whenInventoryFails(InventoryEvent inventoryEvent, UserTransaction userTransaction) {
        userTransaction.setInventoryStatus( InventoryStatus.INVENTORY_FAILED );
        return getPaymentEvent( inventoryEvent, PaymentStatus.PAYMENT_CANCELED );
    }
    
    private Mono<PaymentEvent> whenInventoryComplete(InventoryEvent inventoryEvent, UserTransaction userTransaction) {
        if (userTransaction.getCustomerStatus() != null && userTransaction.getCustomerStatus().equals( CustomerStatus.CUSTOMER_COMPLETED )) {
            return getPaymentEvent( inventoryEvent, PaymentStatus.PAYMENT_COMPLETED );
        }
        if (userTransaction.getCustomerStatus() != null && userTransaction.getCustomerStatus().equals( CustomerStatus.CUSTOMER_FAILED )) {
            return getPaymentEvent( inventoryEvent, PaymentStatus.PAYMENT_CANCELED );
        }
        return null;
    }
    
    private UserTransaction getUser(Integer orderId) {
        return userTransactionRepository.findById( orderId ).orElse( null );
    }
    
    private void logService(String s, Integer step, String tranId) {
        String log = MessageFormat.format( "{0}", s );
        logService.saveLog( step + 1, tranId, log );
    }
    
    private Mono<PaymentEvent> processPayment(CustomerEvent customerEvent) {
        
        logService( customerEvent.toString(), customerEvent.getStep(), customerEvent.getTranId() );
        if (customerEvent.getCustomerStatus().equals( CustomerStatus.CUSTOMER_COMPLETED )){
            
            Mono<PaymentEvent> customerResult = whenCustomerComplete( customerEvent );
            if (customerResult != null) return customerResult;
    
    
        }if(customerEvent.getCustomerStatus().equals( CustomerStatus.CUSTOMER_FAILED )) {
           return whenCustomerFails( customerEvent );
        }
        return null;
    }
    
    private Mono<PaymentEvent> whenCustomerComplete(CustomerEvent customerEvent) {
        
        UserTransaction userTransaction = getUser( customerEvent.getCustomerRequestDto().getOrderId() );
        
        if(userTransaction!=null) {
            changeStateWhenCustomerComplete( userTransaction );
            if (userTransaction.getInventoryStatus() != null &&
            userTransaction.getInventoryStatus().equals( InventoryStatus.INVENTORY_COMPLETED )
            ) {
                return getPaymentEvent( customerEvent, PaymentStatus.PAYMENT_COMPLETED );
            } else {
                return Mono.fromRunnable( () -> {
                } );
            }
        }
        return null;
    }
    
    private void changeStateWhenCustomerComplete(UserTransaction userTransaction) {
        userTransaction.setPaymentStatus( PaymentStatus.PAYMENT_COMPLETED );
        userTransaction.setCustomerStatus( CustomerStatus.CUSTOMER_COMPLETED );
        userTransactionRepository.save( userTransaction );
    }
    
    private Mono<PaymentEvent> whenCustomerFails(CustomerEvent customerEvent) {
        
        UserTransaction userTransaction = getUser( customerEvent.getCustomerRequestDto().getOrderId() );
        if(userTransaction!=null) {
            changeUTWhenCustomerFails( userTransaction );
            return getPaymentEvent( customerEvent, PaymentStatus.PAYMENT_CANCELED );
        }
        return null;
        
    }
    
    private void changeUTWhenCustomerFails(UserTransaction userTransaction) {
        userTransaction.setPaymentStatus( PaymentStatus.PAYMENT_CANCELED );
        userTransaction.setCustomerStatus( CustomerStatus.CUSTOMER_FAILED );
        userTransactionRepository.save( userTransaction);
    }
    
    private Mono<PaymentEvent> processPayment(OrderEvent orderEvent){
        
        logService( orderEvent.toString(), orderEvent.getStep(), orderEvent.getTranId() );
        
        if(orderEvent.getOrderStatus().equals( OrderStatus.ORDER_CREATED )) {
            return whenOrderCreated( orderEvent );
        }
        
        if(orderEvent.getOrderStatus().equals( OrderStatus.ORDER_CANCELLED )){
            return whenOrderCanceled( orderEvent );
        }
        
        return null;
        
    }
    
    private Mono<PaymentEvent> whenOrderCanceled(OrderEvent orderEvent) {
        UserTransaction ut = getUser( orderEvent.getOrderRequestDto().getOrderId() );
        if(ut!=null) {
            boolean inventoryStatus=
            ut.getInventoryStatus() == InventoryStatus.INVENTORY_COMPLETED;
            boolean customerStatus;
            customerStatus = ut.getCustomerStatus() == CustomerStatus.CUSTOMER_COMPLETED;
            changeStateOfUser( ut );
            
            return getPaymentEvent( orderEvent, PaymentStatus.PAYMENT_CANCELED, inventoryStatus
            , customerStatus );
        }
        return null;
    }
    
    private void changeStateOfUser(UserTransaction ut) {
        ut.setPaymentStatus( PaymentStatus.PAYMENT_CANCELED );
        ut.setCustomerStatus( CustomerStatus.CUSTOMER_FAILED );
        ut.setInventoryStatus( InventoryStatus.INVENTORY_FAILED );
        userTransactionRepository.save( ut );
    }
    
    private Mono<PaymentEvent> whenOrderCreated(OrderEvent orderEvent) {
        OrderRequestDto ord= orderEvent.getOrderRequestDto();
        userTransactionRepository
        .save(UserTransaction
              .builder()
              .userId( ord.getUserId() )
              .orderId( ord.getOrderId() )
              .amount( ord.getAmount() )
              .paymentStatus( PaymentStatus.PAYMENT_CREATED )
              .build()
        );
        return getPaymentEvent( orderEvent, PaymentStatus.PAYMENT_CREATED, false, false );
    }
    
    private Mono<PaymentEvent> getPaymentEvent(CustomerEvent customerEvent, PaymentStatus paymentStatus) {
        PaymentRequestDto paymentRequestDto=PaymentRequestDto
        .builder().amount((int) customerEvent.getCustomerRequestDto().getAmount() )
        .orderId( customerEvent.getCustomerRequestDto().getOrderId() )
        .productId( customerEvent.getCustomerRequestDto().getProductId() )
        .userId( customerEvent.getCustomerRequestDto().getUserId()).build();
        PaymentEvent paymentEvent=PaymentEvent.builder()
        .eventId( UUID.randomUUID() )
        .step( customerEvent.getStep()+1 )
        .eventDate( new Date() )
        .tranId( customerEvent.getTranId() )
        .paymentStatus( paymentStatus)
        .paymentRequestDto( paymentRequestDto ).build();
        
        return Mono.fromSupplier(()-> paymentEvent );
    }
    
    private Mono<PaymentEvent> getPaymentEvent(InventoryEvent inventoryEvent, PaymentStatus paymentStatus) {
        PaymentRequestDto paymentRequestDto=PaymentRequestDto
        .builder().amount(inventoryEvent.getInventoryRequestDto().getAmount() )
        .orderId( inventoryEvent.getInventoryRequestDto().getOrderId() )
        .productId( inventoryEvent.getInventoryRequestDto().getProductId() )
        .userId( inventoryEvent.getInventoryRequestDto().getUserId()).build();
        PaymentEvent paymentEvent=PaymentEvent.builder()
        .eventId( UUID.randomUUID() )
        .step( inventoryEvent.getStep()+1 )
        .eventDate( new Date() )
        .tranId( inventoryEvent.getTranId() )
        .paymentStatus( paymentStatus)
        .paymentRequestDto( paymentRequestDto ).build();
        
        return Mono.fromSupplier(()-> paymentEvent );
    }
    
    private Mono<PaymentEvent> getPaymentEvent(OrderEvent orderEvent,
                                               PaymentStatus paymentStatus,
                                               boolean inventoryStatus, boolean customerStatus) {
        PaymentRequestDto paymentRequestDto = PaymentRequestDto
        .builder().amount( orderEvent.getOrderRequestDto().getAmount() )
        .orderId( orderEvent.getOrderRequestDto().getOrderId() )
        .productId( orderEvent.getOrderRequestDto().getProductId() )
        .userId( orderEvent.getOrderRequestDto().getUserId() ).build();
        PaymentEvent paymentEvent = PaymentEvent.builder()
        .paymentStatus( paymentStatus )
        .inventoryStatus( inventoryStatus )
        .customerStatus( customerStatus )
        .eventId( UUID.randomUUID() )
        .step( orderEvent.getStep() + 1 )
        .tranId( orderEvent.getTranId() )
        .eventDate( new Date() )
        .paymentRequestDto( paymentRequestDto ).build();
        return Mono.fromSupplier( ()-> paymentEvent );
    }
}
