package com.sample.saga.orders.log;
import com.sample.saga.commons.dto.LogManagementDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends MongoRepository<LogManagementDto,String> {
}
