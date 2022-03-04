package com.sample.saga.payment.service;

import com.sample.saga.payment.entity.UserBalance;
import com.sample.saga.payment.repository.UserBalanceRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PaymentService {
    
    private final UserBalanceRepository userBalanceRepository;
    
    public PaymentService(UserBalanceRepository userBalanceRepository) {
        this.userBalanceRepository = userBalanceRepository;
    }
    
    @PostConstruct
    public void initUserBalanceInDB() {
        userBalanceRepository.saveAll(Stream.of(new UserBalance(101, 5000),
                                                new UserBalance(102, 3000),
                                                new UserBalance(103, 4200),
                                                new UserBalance(104, 20000),
                                                new UserBalance(105, 999)).collect(Collectors.toList()));
    }
}
