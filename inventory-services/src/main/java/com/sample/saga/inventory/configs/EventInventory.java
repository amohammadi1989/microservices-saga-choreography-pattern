package com.sample.saga.inventory.configs;
import com.sample.saga.commons.dto.InventoryRequestDto;
import com.sample.saga.commons.event.InventoryEvent;
import com.sample.saga.commons.event.InventoryStatus;
import com.sample.saga.commons.event.PaymentEvent;
import com.sample.saga.commons.event.PaymentStatus;
import com.sample.saga.inventory.dtos.ProductEntity;
import com.sample.saga.inventory.log.LogService;
import com.sample.saga.inventory.services.ProductService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Configuration
public class EventInventory {
  
  private final LogService logService;
  
  private final ProductService productService;
  
  
  public EventInventory(LogService logService, ProductService productService) {
    this.logService = logService;
    this.productService = productService;
  }
  
  @Bean
  public Function<Flux<PaymentEvent>,Flux<InventoryEvent>> inventoryEventConsumer(){
    return inventoryEventFlux -> inventoryEventFlux.flatMap( this::paymentProcess );
  }
  
  public Mono<InventoryEvent> paymentProcess(PaymentEvent paymentEvent){
    logManagement( paymentEvent );
    
    ProductEntity productEntity = getProduct( paymentEvent );
    
    if(paymentEvent.getPaymentStatus().equals( PaymentStatus.PAYMENT_CREATED )) {
      return whenPaymentCreated( paymentEvent, productEntity );
    }
    if(paymentEvent.getPaymentStatus().equals( PaymentStatus.PAYMENT_CANCELED )){
      whenPaymentCanceled( paymentEvent, productEntity );
    }
    return Mono.fromRunnable( ()->{});
  }
  
  private ProductEntity getProduct(PaymentEvent paymentEvent) {
    return productService.findById( paymentEvent.getPaymentRequestDto().getProductId() );
  }
  
  private void whenPaymentCanceled(PaymentEvent paymentEvent, ProductEntity productEntity) {
    if(paymentEvent.isInventoryStatus()){
      productEntity.setId( paymentEvent.getPaymentRequestDto().getProductId() );
      productEntity.setCount( productEntity.getCount()+1 );
      productService.save( productEntity );
    }
  }
  
  private Mono<InventoryEvent> whenPaymentCreated(PaymentEvent paymentEvent, ProductEntity productEntity) {
    InventoryStatus inventoryStatus = InventoryStatus.INVENTORY_FAILED;
    if (productEntity != null) {
      inventoryStatus = changeStateOfProduct( productEntity, inventoryStatus );
    }
    InventoryEvent inventoryEvent = getInventoryEvent( paymentEvent, inventoryStatus );
    return Mono.fromSupplier( () -> inventoryEvent );
  }
  
  private InventoryStatus changeStateOfProduct(ProductEntity productEntity, InventoryStatus inventoryStatus) {
    if (productEntity.getCount() > 0) {
      productEntity.setCount( productEntity.getCount() - 1 );
      productService.save( productEntity );
      inventoryStatus = InventoryStatus.INVENTORY_COMPLETED;
    }
    return inventoryStatus;
  }
  
  private void logManagement(PaymentEvent paymentEvent) {
    String log= MessageFormat.format( "{0}",
                                      paymentEvent.toString() ) ;
    
    logService.saveLog( paymentEvent.getStep()+1, paymentEvent.getTranId(),
                        log);
  }
  
  private InventoryEvent getInventoryEvent(PaymentEvent paymentEvent,InventoryStatus inventoryStatus) {
    InventoryRequestDto inventoryRequestDto=InventoryRequestDto.builder()
    .productId( paymentEvent.getPaymentRequestDto().getProductId() )
    .orderId( paymentEvent.getPaymentRequestDto().getOrderId() )
    .amount( paymentEvent.getPaymentRequestDto().getAmount() )
    .userId( paymentEvent.getPaymentRequestDto().getUserId() )
    .build();
    return InventoryEvent.builder()
    .eventId( UUID.randomUUID() )
    .eventDate( new Date() )
    .tranId( paymentEvent.getTranId() )
    .step( paymentEvent.getStep()+1 )
    .inventoryRequestDto( inventoryRequestDto )
    .inventoryStatus( inventoryStatus )
    .build();
  }
}
