package com.quickmart.cash;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cash")
public class CashController {

    @Autowired
    private CashService service;

    /**
     * POST /api/cash/inbound
     * Cash coming INTO the store (sales, deposits, opening float)
     */
    @PostMapping("/inbound")
    public ResponseEntity<CashTransaction> inbound(
            @RequestBody CashTransaction request) {
        CashTransaction result = service.processInbound(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * POST /api/cash/outbound
     * Cash going OUT of the store (refunds, pickups)
     */
    @PostMapping("/outbound")
    public ResponseEntity<CashTransaction> outbound(
            @RequestBody CashTransaction request) {
        CashTransaction result = service.processOutbound(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * POST /api/cash/loan
     * Temporary cash loan to store/till
     */
    @PostMapping("/loan")
    public ResponseEntity<CashTransaction> loan(
            @RequestBody CashTransaction request) {
        CashTransaction result = service.processCashLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * POST /api/cash/pickup
     * Cash pickup from store
     */
    @PostMapping("/pickup")
    public ResponseEntity<CashTransaction> pickup(
            @RequestBody CashTransaction request) {
        CashTransaction result = service.processCashPickup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * GET /api/cash/store/{storeId}
     * Get all cash transactions for a store
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<CashTransaction>> getByStore(
            @PathVariable String storeId) {
        return ResponseEntity.ok(service.getByStore(storeId));
    }

    /**
     * GET /api/cash/{transactionId}
     * Get specific cash transaction
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<CashTransaction> getById(
            @PathVariable String transactionId) {
        CashTransaction result = service.getByTransactionId(transactionId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}