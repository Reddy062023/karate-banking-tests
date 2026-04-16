package com.quickmart.businessday;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessDayRepository
        extends MongoRepository<BusinessDay, String> {
    Optional<BusinessDay> findByBusinessDayId(String businessDayId);
    Optional<BusinessDay> findByStoreIdAndStatus(String storeId, String status);
    List<BusinessDay> findByStoreId(String storeId);
    Optional<BusinessDay> findByStoreIdAndTradingDate(
            String storeId, String tradingDate);
}