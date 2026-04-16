package com.quickmart.till;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TillService {

    @Autowired
    private TillRepository repository;

    /**
     * Open Till - start of day cash setup
     * In Instore: cashier opens till with opening float
     */
    public Till openTill(Till request) {
        // Check if till already open for this store
        repository.findByStoreIdAndStatus(request.getStoreId(), "OPEN")
                .ifPresent(t -> {
                    throw new RuntimeException("Till already open for store: "
                            + request.getStoreId());
                });

        request.setTillId("TILL-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase());
        request.setStatus("OPEN");
        request.setCurrentBalance(request.getOpeningBalance());
        request.setOpenedAt(Instant.now().toString());
        request.setLastUpdated(Instant.now().toString());

        System.out.println("Till opened: " + request.getTillId()
                + " Store: " + request.getStoreId()
                + " Opening balance: " + request.getOpeningBalance());

        return repository.save(request);
    }

    /**
     * Close Till - end of day closing
     * In Instore: cashier closes till with final cash count
     */
    public Till closeTill(String tillId, Till request) {
        Till till = repository.findByTillId(tillId)
                .orElseThrow(() -> new RuntimeException(
                        "Till not found: " + tillId));

        if (!till.getStatus().equals("OPEN")) {
            throw new RuntimeException("Till is not open: " + tillId);
        }

        till.setStatus("CLOSED");
        till.setClosingBalance(request.getClosingBalance());
        till.setClosedAt(Instant.now().toString());
        till.setLastUpdated(Instant.now().toString());

        System.out.println("Till closed: " + tillId
                + " Closing balance: " + request.getClosingBalance());

        return repository.save(till);
    }

    /**
     * Get Till Status
     * In Instore: check current state of cash drawer
     */
    public Till getTillStatus(String tillId) {
        return repository.findByTillId(tillId)
                .orElseThrow(() -> new RuntimeException(
                        "Till not found: " + tillId));
    }

    /**
     * Get all tills for a store
     */
    public List<Till> getTillsByStore(String storeId) {
        return repository.findByStoreId(storeId);
    }

    /**
     * Suspend Till - temporarily disable
     */
    public Till suspendTill(String tillId) {
        Till till = repository.findByTillId(tillId)
                .orElseThrow(() -> new RuntimeException(
                        "Till not found: " + tillId));
        till.setStatus("SUSPENDED");
        till.setLastUpdated(Instant.now().toString());
        return repository.save(till);
    }
}