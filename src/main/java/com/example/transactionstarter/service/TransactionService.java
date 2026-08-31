package com.example.transactionstarter.service;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.entity.Transaction;
import com.example.transactionstarter.enums.TransactionStatus;
import com.example.transactionstarter.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import com.example.transactionstarter.exception.DuplicateTransactionException;
import com.example.transactionstarter.exception.TransactionNotFoundException;
import com.example.transactionstarter.dto.UpdateStatusRequest;
import com.example.transactionstarter.exception.InvalidStatusTransitionException;
import java.util.List;
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction createTransaction(CreateTransactionRequest request) {

        if (transactionRepository.existsById(request.getTransactionId())) {
            throw new DuplicateTransactionException("Transaction ID already exists");
        }

        Transaction transaction = new Transaction();

        transaction.setTransactionId(request.getTransactionId());
        transaction.setCustomerId(request.getCustomerId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setTransactionType(request.getTransactionType());

        // Every new transaction starts as PENDING
        transaction.setTransactionStatus(TransactionStatus.PENDING);

        return transactionRepository.save(transaction);
    }
    public Transaction getTransaction(String transactionId) {

        return transactionRepository.findById(transactionId)
                    .orElseThrow(() ->
                    new TransactionNotFoundException(
                            "Transaction not found: " + transactionId
                    ));
    }
    public Transaction updateTransactionStatus(
        String transactionId,
        UpdateStatusRequest request) {

            Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() ->
                    new TransactionNotFoundException(
                            "Transaction not found: " + transactionId
                    ));

        TransactionStatus currentStatus = transaction.getTransactionStatus();
        TransactionStatus newStatus = request.getStatus();

        if (currentStatus != TransactionStatus.PENDING
        || newStatus == TransactionStatus.PENDING) {


        throw new InvalidStatusTransitionException(

            "Invalid status transition from "
                + currentStatus
                + " to "
                + newStatus
            );
        }

        transaction.setTransactionStatus(newStatus);

        return transactionRepository.save(transaction);
    }
    public List<Transaction> getTransactionsByCustomer(String customerId) {

        return transactionRepository.findByCustomerId(customerId);
    }
}