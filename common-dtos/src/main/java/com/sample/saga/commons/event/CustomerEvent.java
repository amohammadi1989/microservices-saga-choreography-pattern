package com.sample.saga.commons.event;
import com.sample.saga.commons.dto.CustomerRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerEvent implements Event{
  private UUID eventId=UUID.randomUUID();
  private Date eventDate=new Date();
  private Integer step;
  private String tranId;
  private CustomerRequestDto customerRequestDto;
  private CustomerStatus customerStatus;
  @Override
  public UUID getEventId() {
    return eventId;
  }
  
  @Override
  public Date getDate() {
    return eventDate;
  }
  
  public CustomerEvent(CustomerRequestDto customerRequestDto, CustomerStatus customerStatus) {
    this.customerRequestDto = customerRequestDto;
    this.customerStatus = customerStatus;
  }
  
  @Override
  public String toString() {
    return "CustomerEvent{" +
    " status=" + customerStatus +
    ",dto=" + customerRequestDto+
    '}';
  }
}
