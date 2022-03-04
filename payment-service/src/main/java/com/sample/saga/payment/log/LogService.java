package com.sample.saga.payment.log;
import com.sample.saga.commons.dto.LogManagementDto;
import org.springframework.stereotype.Service;

@Service
public class LogService {
  
  final LogRepository repository;
  
  public LogService(LogRepository repository) {
    this.repository = repository;
  }
  
  public void saveLog(Integer step, String transactionId, String log){
    LogManagementDto managementDto=LogManagementDto.builder()
    .log( log )
    .step( step )
    .transactionId( transactionId ).build();
    repository.save( managementDto );
  }
}
