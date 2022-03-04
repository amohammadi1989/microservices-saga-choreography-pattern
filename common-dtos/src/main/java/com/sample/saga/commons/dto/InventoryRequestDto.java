package com.sample.saga.commons.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequestDto {
  private Integer productId;
  private Integer orderId;
  private Integer userId;
  private Integer amount;
}
