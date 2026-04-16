package com.quickmart.cash;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CashRepository extends MongoRepository<CashTransaction, String> {
    List<CashTransaction> findByStoreId(String storeId);
    List<CashTransaction> findByType(String type);
    CashTransaction findByTransactionId(String transactionId);
}