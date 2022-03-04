package com.sample.saga.customer.services;
import com.sample.saga.customer.dtos.Customer;
import com.sample.saga.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CustomerService {
  
  final CustomerRepository customerRepository;
  
  public CustomerService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }
  
  @PostConstruct
  public void init(){
    createCustomer( "Ali","Mohammadi",8000000,true );
    createCustomer( "AliReza","Karimi",5000000,true );
    createCustomer( "Reza","Sajadi",4000000,true );
    createCustomer( "Said","Jafari",3000000,true );
    createCustomer( "Jafar","Moradi",2000000,true );
    createCustomer( "Karim","Hossini",1000000,true );
  }
  public void createCustomer(String name,String lastName,double balance,boolean state){
    Customer customer=
    Customer
    .builder()
    .balance( balance )
    .state( state )
    .name( name )
    .lastName( lastName )
    .build();
    customerRepository.save( customer );
  }
  public void updateCustomer(Customer customer){
    customerRepository.save( customer );
  }
  public Customer findById(Integer id){
    return  customerRepository.findById( id ).orElse( null );
  }
}
