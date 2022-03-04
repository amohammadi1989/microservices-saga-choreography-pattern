package com.sample.saga.commons.dto;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.annotation.Documented;
import java.util.PrimitiveIterator;

@Data
@Builder
@Document(collection = "LogManagement")
public class LogManagementDto {
  @Id
  private String id;
  private Integer step;
  private String transactionId;
  private String log;
}
