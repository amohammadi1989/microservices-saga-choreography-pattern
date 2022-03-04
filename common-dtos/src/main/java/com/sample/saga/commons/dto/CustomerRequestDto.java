package com.sample.saga.commons.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequestDto {
  private Integer userId;
  private double amount;
  private Integer orderId;
  private Integer productId;
}
