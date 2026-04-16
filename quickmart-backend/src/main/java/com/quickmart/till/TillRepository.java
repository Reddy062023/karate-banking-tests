package com.quickmart.till;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TillRepository extends MongoRepository<Till, String> {
    Optional<Till> findByTillId(String tillId);
    List<Till> findByStoreId(String storeId);
    Optional<Till> findByStoreIdAndStatus(String storeId, String status);
}