package com.quickmart.businessday;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class BusinessDayService {

    @Autowired
    private BusinessDayRepository repository;

    /**
     * Create Business Day - start of trading day
     * In Instore: store manager opens business day
     * before any transactions can happen
     */
    public BusinessDay createBusinessDay(BusinessDay request) {

        // Check if business day already open for this store
        repository.findByStoreIdAndStatus(request.getStoreId(), "OPEN")
                .ifPresent(bd -> {
                    throw new RuntimeException(
                            "Business day already open for store: "
                            + request.getStoreId());
                });

        request.setBusinessDayId("BD-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase());
        request.setStatus("OPEN");
        request.setTradingDate(LocalDate.now().toString());
        request.setStartTime(Instant.now().toString());
        request.setTotalSales(0.0);
        request.setTotalRefunds(0.0);

        System.out.println("Business day created: "
                + request.getBusinessDayId()
                + " Store: " + request.getStoreId()
                + " Date: " + request.getTradingDate());

        return repository.save(request);
    }

    /**
     * End Business Day - close trading day
     * In Instore: store manager closes business day
     * after all transactions reconciled
     */
    public BusinessDay endBusinessDay(String businessDayId,
            BusinessDay request) {

        BusinessDay bd = repository.findByBusinessDayId(businessDayId)
                .orElseThrow(() -> new RuntimeException(
                        "Business day not found: " + businessDayId));

        if (!bd.getStatus().equals("OPEN")) {
            throw new RuntimeException(
                    "Business day is not open: " + businessDayId);
        }

        bd.setStatus("CLOSED");
        bd.setClosingFloat(request.getClosingFloat());
        bd.setTotalSales(request.getTotalSales());
        bd.setTotalRefunds(request.getTotalRefunds());
        bd.setEndTime(Instant.now().toString());
        bd.setClosedBy(request.getClosedBy());

        System.out.println("Business day closed: " + businessDayId
                + " Total sales: " + request.getTotalSales()
                + " Total refunds: " + request.getTotalRefunds());

        return repository.save(bd);
    }

    /**
     * Get Business Day status
     */
    public BusinessDay getBusinessDay(String businessDayId) {
        return repository.findByBusinessDayId(businessDayId)
                .orElseThrow(() -> new RuntimeException(
                        "Business day not found: " + businessDayId));
    }

    /**
     * Get current open business day for a store
     */
    public BusinessDay getCurrentBusinessDay(String storeId) {
        return repository.findByStoreIdAndStatus(storeId, "OPEN")
                .orElseThrow(() -> new RuntimeException(
                        "No open business day for store: " + storeId));
    }

    /**
     * Get all business days for a store
     */
    public List<BusinessDay> getBusinessDaysByStore(String storeId) {
        return repository.findByStoreId(storeId);
    }
}