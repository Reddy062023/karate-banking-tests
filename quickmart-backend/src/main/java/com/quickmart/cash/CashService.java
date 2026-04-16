package com.quickmart.cash;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CashService {

    @Autowired
    private CashRepository repository;

    /**
     * Cash Inbound - money coming INTO the store
     * Examples: Cash sale, opening float, cash deposit
     */
    public CashTransaction processInbound(CashTransaction request) {
        request.setTransactionId("INBOUND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        request.setType("INBOUND");
        request.setStatus("PROCESSED");
        request.setTimestamp(Instant.now().toString());
        request.setProcessedAt(Instant.now().toString());
        return repository.save(request);
    }

    /**
     * Cash Outbound - money going OUT of the store
     * Examples: Refund, cash pickup, supplier payment
     */
    public CashTransaction processOutbound(CashTransaction request) {
        request.setTransactionId("OUTBOUND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        request.setType("OUTBOUND");
        request.setStatus("PROCESSED");
        request.setTimestamp(Instant.now().toString());
        request.setProcessedAt(Instant.now().toString());
        return repository.save(request);
    }

    /**
     * Cash Loan - temporary cash given to store/till
     * Example: Extra float needed during busy period
     */
    public CashTransaction processCashLoan(CashTransaction request) {
        request.setTransactionId("LOAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        request.setType("LOAN");
        request.setReason("CASH_LOAN");
        request.setStatus("PROCESSED");
        request.setTimestamp(Instant.now().toString());
        request.setProcessedAt(Instant.now().toString());
        return repository.save(request);
    }

    /**
     * Cash Pickup - cash removed from store for bank/office
     * Example: End of day security collection
     */
    public CashTransaction processCashPickup(CashTransaction request) {
        request.setTransactionId("PICKUP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        request.setType("PICKUP");
        request.setReason("CASH_PICKUP");
        request.setStatus("PROCESSED");
        request.setTimestamp(Instant.now().toString());
        request.setProcessedAt(Instant.now().toString());
        return repository.save(request);
    }

    public List<CashTransaction> getByStore(String storeId) {
        return repository.findByStoreId(storeId);
    }

    public CashTransaction getByTransactionId(String transactionId) {
        return repository.findByTransactionId(transactionId);
    }
}