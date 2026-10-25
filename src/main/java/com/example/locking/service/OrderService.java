package com.example.locking.service;

import com.example.locking.enitity.Product;
import com.example.locking.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final ProductRepository productRepository;

    @PostConstruct
    public void seedProducts() {
        log.info("Seeding products");

        Product product = new Product();
        product.setId(1L);
        product.setName("pen");
        product.setStock(10);

        productRepository.save(product);
    }

    public ResponseEntity<String> processOrderPesimisticLock(
            HashMap<String, String> orderDetailMap,
            Boolean buy) {

        log.info("Entered pessimistic lock processing path");

        int quantity = Integer.parseInt(orderDetailMap.get("quantity"));
        Long id = Long.parseLong(orderDetailMap.get("id"));

        if (!buy) {
            return processRelease(id, quantity);
        }

        return processBuyPesimistic(id, quantity);
    }

    public ResponseEntity<String> processOrderOptimisticLock(
            HashMap<String, String> orderDetailMap,
            Boolean buy) {

        log.info("Entered optimistic lock processing path");

        int quantity = Integer.parseInt(orderDetailMap.get("quantity"));
        Long id = Long.parseLong(orderDetailMap.get("id"));

        if (!buy) {
            return processRelease(id, quantity);
        }

        return processBuyOptimistic(id, quantity);
    }

    @Transactional
    public ResponseEntity<String> processRelease(Long id, int quantity) {

        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setStock(product.getStock() + quantity);

            productRepository.save(product);

            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body("processing");

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<String> processBuyPesimistic(Long id, int quantity) {

        try {
            Product product = productRepository
                    .findByIdForUpdatePesimistic(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            int currentStock = product.getStock();

            if (currentStock < quantity) {
                throw new RuntimeException("Products aren't enough");
            }

            product.setStock(currentStock - quantity);

            productRepository.save(product);

            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body("processing");

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<String> processBuyOptimistic(Long id, int quantity) {

        try {
            Product product = productRepository
                    .findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getStock() < quantity) {
                throw new RuntimeException("Products aren't enough");
            }

            product.setStock(product.getStock() - quantity);

            productRepository.save(product);

            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body("processing");

        } catch (OptimisticLockException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Concurrent update detected. Please retry.");

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(e.getMessage());
        }
    }
}