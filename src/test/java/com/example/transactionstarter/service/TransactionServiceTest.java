package com.example.transactionstarter.service;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.entity.Transaction;
import com.example.transactionstarter.enums.TransactionStatus;
import com.example.transactionstarter.enums.TransactionType;
import com.example.transactionstarter.exception.DuplicateTransactionException;
import com.example.transactionstarter.exception.TransactionNotFoundException;
import com.example.transactionstarter.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.transactionstarter.dto.UpdateStatusRequest;
import com.example.transactionstarter.exception.InvalidStatusTransitionException;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldCreateTransactionSuccessfully() {

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setTransactionId("TXN001");
        request.setCustomerId("CUS001");
        request.setAmount(new BigDecimal("1000.50"));
        request.setCurrency("INR");
        request.setTransactionType(TransactionType.PAYMENT);

        when(transactionRepository.existsById("TXN001")).thenReturn(false);

        Transaction savedTransaction = new Transaction(
                "TXN001",
                "CUS001",
                new BigDecimal("1000.50"),
                "INR",
                TransactionType.PAYMENT,
                TransactionStatus.PENDING
        );

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(savedTransaction);

        Transaction result = transactionService.createTransaction(request);

        assertNotNull(result);
        assertEquals("TXN001", result.getTransactionId());
        assertEquals("CUS001", result.getCustomerId());
        assertEquals(new BigDecimal("1000.50"), result.getAmount());
        assertEquals("INR", result.getCurrency());
        assertEquals(TransactionType.PAYMENT, result.getTransactionType());
        assertEquals(TransactionStatus.PENDING, result.getTransactionStatus());

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldRejectDuplicateTransactionId() {

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setTransactionId("TXN001");
        request.setCustomerId("CUS001");
        request.setAmount(new BigDecimal("1000.50"));
        request.setCurrency("INR");
        request.setTransactionType(TransactionType.PAYMENT);

        when(transactionRepository.existsById("TXN001")).thenReturn(true);

        assertThrows(
                DuplicateTransactionException.class,
                () -> transactionService.createTransaction(request)
        );

        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    @Test
    void shouldGetTransactionSuccessfully() {

        Transaction transaction = new Transaction(
                "TXN001",
                "CUS001",
                new BigDecimal("1000.50"),
                "INR",
                TransactionType.PAYMENT,
                TransactionStatus.PENDING
        );

        when(transactionRepository.findById("TXN001"))
                .thenReturn(java.util.Optional.of(transaction));

        Transaction result = transactionService.getTransaction("TXN001");

        assertNotNull(result);
        assertEquals("TXN001", result.getTransactionId());
        assertEquals("CUS001", result.getCustomerId());
        assertEquals(TransactionStatus.PENDING, result.getTransactionStatus());

        verify(transactionRepository).findById("TXN001");
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {

        when(transactionRepository.findById("TXN999"))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.getTransaction("TXN999")
        );

        verify(transactionRepository).findById("TXN999");
    }
    @Test
    void shouldUpdateTransactionStatusSuccessfully() {

        Transaction transaction = new Transaction(
            "TXN001",
            "CUS001",
            new BigDecimal("1000.50"),
            "INR",
            TransactionType.PAYMENT,
            TransactionStatus.PENDING
        );

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(TransactionStatus.COMPLETED);

        when(transactionRepository.findById("TXN001"))
                .thenReturn(java.util.Optional.of(transaction));

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        Transaction result =
                transactionService.updateTransactionStatus("TXN001", request);

        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED,
                result.getTransactionStatus());

        verify(transactionRepository).findById("TXN001");
        verify(transactionRepository).save(transaction);
    }
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingTransaction() {

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(TransactionStatus.COMPLETED);

        when(transactionRepository.findById("TXN999"))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.updateTransactionStatus(
                        "TXN999", request)
        );

        verify(transactionRepository).findById("TXN999");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    @Test
    void shouldRejectInvalidStatusTransition() {

        Transaction transaction = new Transaction(
                "TXN001",
                "CUS001",
                new BigDecimal("1000.50"),
                "INR",
                TransactionType.PAYMENT,
                TransactionStatus.COMPLETED
        );

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(TransactionStatus.FAILED);

        when(transactionRepository.findById("TXN001"))
                .thenReturn(java.util.Optional.of(transaction));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> transactionService.updateTransactionStatus(
                        "TXN001", request)
        );

        verify(transactionRepository).findById("TXN001");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    @Test
    void shouldGetTransactionsForCustomer() {

        Transaction transaction1 = new Transaction(
            "TXN101",
            "CUS100",
            new BigDecimal("500.00"),
            "INR",
            TransactionType.PAYMENT,
            TransactionStatus.PENDING
        );

        Transaction transaction2 = new Transaction(
            "TXN102",
            "CUS100",
            new BigDecimal("750.00"),
            "INR",
            TransactionType.PAYMENT,
            TransactionStatus.PENDING
        );

        when(transactionRepository.findByCustomerId("CUS100"))
            .thenReturn(java.util.List.of(transaction1, transaction2));

        java.util.List<Transaction> result =
            transactionService.getTransactionsByCustomer("CUS100");

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("TXN101", result.get(0).getTransactionId());
        assertEquals("TXN102", result.get(1).getTransactionId());

        verify(transactionRepository).findByCustomerId("CUS100");
    }
    @Test
    void shouldReturnEmptyListWhenCustomerHasNoTransactions() {

        when(transactionRepository.findByCustomerId("CUS999"))
            .thenReturn(java.util.List.of());

        java.util.List<Transaction> result =
            transactionService.getTransactionsByCustomer("CUS999");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(transactionRepository).findByCustomerId("CUS999");
    }
    @Test
    void shouldRejectTransactionWithInvalidAmount() {


        CreateTransactionRequest request = new CreateTransactionRequest();

        request.setTransactionId("TXN003");
        request.setCustomerId("CUS003");
        request.setAmount(new BigDecimal("0"));
        request.setCurrency("INR");
        request.setTransactionType(TransactionType.PAYMENT);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<CreateTransactionRequest>> violations =
            validator.validate(request);

        assertFalse(violations.isEmpty());

        assertTrue(
            violations.stream()
                    .anyMatch(v -> v.getPropertyPath()
                            .toString()
                            .equals("amount"))
        );

        factory.close();
    }
    @Test
    void shouldRejectPendingToPendingStatusTransition() {

        Transaction transaction = new Transaction(

            "TXN001",
            "CUS001",
            new BigDecimal("1000.50"),
            "INR",
            TransactionType.PAYMENT,
            TransactionStatus.PENDING
        );

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(TransactionStatus.PENDING);

        when(transactionRepository.findById("TXN001"))
            .thenReturn(java.util.Optional.of(transaction));

        assertThrows(
            InvalidStatusTransitionException.class,
            () -> transactionService.updateTransactionStatus(
                    "TXN001", request)
        );

        verify(transactionRepository).findById("TXN001");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
