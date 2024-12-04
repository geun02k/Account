package com.example.account.repository;

import com.example.account.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // transactionId에 해당하는 거래정보 조회
    Optional<Transaction> findByTransactionId(String transactionId);
}
