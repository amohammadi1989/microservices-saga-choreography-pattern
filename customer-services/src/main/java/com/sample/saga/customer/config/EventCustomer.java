package com.sample.saga.customer.config;
import com.sample.saga.commons.dto.CustomerRequestDto;
import com.sample.saga.commons.event.CustomerEvent;
import com.sample.saga.commons.event.CustomerStatus;
import com.sample.saga.commons.event.PaymentEvent;
import com.sample.saga.commons.event.PaymentStatus;
import com.sample.saga.customer.dtos.Customer;
import com.sample.saga.customer.log.LogService;
import com.sample.saga.customer.services.CustomerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Configuration
public class EventCustomer {
  
  final CustomerService customerService;
  final LogService logService;
  
  public EventCustomer(CustomerService customerService, LogService logService) {
    this.customerService = customerService;
    this.logService = logService;
  }
  
  @Bean
  public Function<Flux<PaymentEvent>,Flux<CustomerEvent>> customerProcessor(){
    return customerEventFlux ->customerEventFlux.flatMap(  this::processCustomer );
  }
  
  private Mono<CustomerEvent> processCustomer(PaymentEvent paymentEvent){
    
    logManagement( paymentEvent );
    Customer customer = getCustomer( paymentEvent );
    
    if(paymentEvent.getPaymentStatus().equals( PaymentStatus.PAYMENT_CREATED )) {
      return whenPaymentCreated( paymentEvent, customer );
    }
    
    if(paymentEvent.getPaymentStatus().equals( PaymentStatus.PAYMENT_CANCELED )){
      whenPaymentCanceled( paymentEvent, customer );
      return Mono.fromRunnable(()->{});
    }
    
    return null;
    
  }
  
  private Customer getCustomer(PaymentEvent paymentEvent) {
    return customerService.findById( paymentEvent.getPaymentRequestDto().getUserId() );
  }
  
  private void whenPaymentCanceled(PaymentEvent paymentEvent, Customer customer) {
    if(paymentEvent.isCustomerStatus()){
      customer.setBalance( customer.getBalance()+ paymentEvent.getPaymentRequestDto().getAmount() );
      customerService.updateCustomer( customer );
    }
  }
  
  private Mono<CustomerEvent> whenPaymentCreated(PaymentEvent paymentEvent, Customer customer) {
    if(customer.getBalance()>= paymentEvent.getPaymentRequestDto().getAmount()) {
      changeStateOfCustomer( paymentEvent, customer );
      return getCustomerEvent( paymentEvent, CustomerStatus.CUSTOMER_COMPLETED );
    }else {
      return getCustomerEvent( paymentEvent, CustomerStatus.CUSTOMER_FAILED );
    }
  }
  
  private void changeStateOfCustomer(PaymentEvent paymentEvent, Customer customer) {
    customer.setBalance( customer.getBalance()- paymentEvent.getPaymentRequestDto().getAmount() );
    customerService.updateCustomer( customer );
  }
  
  private void logManagement(PaymentEvent paymentEvent) {
    String log=MessageFormat.format( "{0}",
                                     paymentEvent.toString() ) ;
    
    logService.saveLog( paymentEvent.getStep()+1, paymentEvent.getTranId(), log);
  }
  
  private Mono<CustomerEvent> getCustomerEvent(PaymentEvent paymentEvent, CustomerStatus customerStatus) {
    CustomerRequestDto customerRequestDto=CustomerRequestDto.builder()
    .amount( paymentEvent.getPaymentRequestDto().getAmount() )
    .orderId( paymentEvent.getPaymentRequestDto().getOrderId() )
    .productId( paymentEvent.getPaymentRequestDto().getProductId() )
    .userId( paymentEvent.getPaymentRequestDto().getUserId() ).build();
    CustomerEvent customerEvent=CustomerEvent.builder()
    .eventId( UUID.randomUUID() )
    .eventDate( new Date() )
    .step( paymentEvent.getStep()+1 )
    .tranId( paymentEvent.getTranId() )
    .customerRequestDto( customerRequestDto )
    .customerStatus( customerStatus)
    .build();
    
    return Mono.fromSupplier( ()->customerEvent );
  }
}
