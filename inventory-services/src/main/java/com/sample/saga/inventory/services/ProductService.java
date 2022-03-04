package com.sample.saga.inventory.services;
import com.sample.saga.inventory.dtos.ProductEntity;
import com.sample.saga.inventory.repositoris.ProductRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ProductService {
  
  private final ProductRepository productRepository;
  
  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }
  
  @PostConstruct
  public void init(){
    if(!productRepository.findById( 1 ).isPresent())
      productRepository.save( createProduct(100,"T1" ) );
    if(!productRepository.findById( 2 ).isPresent())
      productRepository.save( createProduct( 10000,"T2" ) );
    if(!productRepository.findById( 3 ).isPresent())
      productRepository.save( createProduct( 30000,"T3" ) );
  }
  private ProductEntity createProduct(int count, String name){
    return ProductEntity.builder().name( name ).count( count ).build();
  }
  public ProductEntity findById(Integer ids){
    return productRepository.findById( ids ).orElse( null );
  }
  public void save(ProductEntity productEntity){
    productRepository.save( productEntity );
  }
}
