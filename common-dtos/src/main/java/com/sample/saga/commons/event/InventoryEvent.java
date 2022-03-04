package com.sample.saga.commons.event;
import com.sample.saga.commons.dto.InventoryRequestDto;
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
public class InventoryEvent implements Event{
  private UUID eventId=UUID.randomUUID();
  private Date eventDate=new Date();
  private Integer step;
  private String tranId;
  private InventoryRequestDto inventoryRequestDto;
  private InventoryStatus inventoryStatus;
  @Override
  public UUID getEventId() {
    return eventId;
  }
  
  @Override
  public Date getDate() {
    return eventDate;
  }
  
  public InventoryEvent(InventoryRequestDto inventoryRequestDto, InventoryStatus inventoryStatus) {
    this.inventoryRequestDto=inventoryRequestDto;
    this.inventoryStatus = inventoryStatus;
  }
  
  @Override
  public String toString() {
    return "InventoryEvent{" +
    " status=" + inventoryStatus +
    ",dto=" + inventoryRequestDto+
    '}';
  }
}
