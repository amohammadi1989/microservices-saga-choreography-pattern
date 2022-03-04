package com.sample.saga.inventory;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
@SpringBootApplication
@ComponentScan("com.sample")
public class InventoryServiceApplication {


    public static void main(String[] args) {
        SpringApplication.run( InventoryServiceApplication.class);
    }
}
