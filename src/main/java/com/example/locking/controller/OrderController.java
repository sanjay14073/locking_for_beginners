package com.example.locking.controller;

import com.example.locking.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/order")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/pesimisticBuy")
    public ResponseEntity<String> processOrderPesimisticLock(@RequestParam HashMap<String,String> orderDetailMap){
        return orderService.processOrderPesimisticLock(orderDetailMap,true);
    }

    @PostMapping("/optimisticBuy")
    public ResponseEntity<String> processOrderOptimisticLock(@RequestParam HashMap<String,String> orderDetailMap){
        return orderService.processOrderOptimisticLock(orderDetailMap,true);
    }

    @DeleteMapping("/pesimisticRelease")
    public ResponseEntity<String> processOrderReleasePesimisticLock(@RequestParam HashMap<String,String> releaseDetailMap){
        return orderService.processOrderPesimisticLock(releaseDetailMap,false);
    }

    @DeleteMapping("/optimisticRelease")
    public ResponseEntity<String> processOrderReleaseOptimistic(@RequestParam HashMap<String,String> releaseDetailMap){
        return orderService.processOrderOptimisticLock(releaseDetailMap,false);
    }
 }
