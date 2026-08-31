package com.example.transactionstarter.controller;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.dto.UpdateStatusRequest;
import com.example.transactionstarter.entity.Transaction;
import com.example.transactionstarter.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
        @Valid @RequestBody CreateTransactionRequest request) {

            Transaction transaction = transactionService.createTransaction(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(

        @PathVariable String transactionId) {

            Transaction transaction =transactionService.getTransaction(transactionId);

        return ResponseEntity.ok(transaction);
    }
    @PatchMapping("/{transactionId}/status")
    public ResponseEntity<Transaction> updateTransactionStatus(

        @PathVariable String transactionId,
        @Valid @RequestBody UpdateStatusRequest request) {

            Transaction transaction =
                transactionService.updateTransactionStatus(transactionId, request);


        return ResponseEntity.ok(transaction);
    }
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Transaction>> getCustomerTransactions(

        @PathVariable String customerId) {


            List<Transaction> transactions =
            transactionService.getTransactionsByCustomer(customerId);

        return ResponseEntity.ok(transactions);
    }
}
