package com.quickmart.till;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/till")
public class TillController {

    @Autowired
    private TillService service;

    /**
     * POST /api/till/open
     * Open a new till for a store
     */
    @PostMapping("/open")
    public ResponseEntity<Till> openTill(@RequestBody Till request) {
        try {
            Till result = service.openTill(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * POST /api/till/{tillId}/close
     * Close an existing till
     */
    @PostMapping("/{tillId}/close")
    public ResponseEntity<Till> closeTill(
            @PathVariable String tillId,
            @RequestBody Till request) {
        try {
            Till result = service.closeTill(tillId, request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * GET /api/till/{tillId}/status
     * Get current till status
     */
    @GetMapping("/{tillId}/status")
    public ResponseEntity<Till> getTillStatus(@PathVariable String tillId) {
        try {
            Till result = service.getTillStatus(tillId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * GET /api/till/store/{storeId}
     * Get all tills for a store
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<Till>> getTillsByStore(
            @PathVariable String storeId) {
        return ResponseEntity.ok(service.getTillsByStore(storeId));
    }

    /**
     * POST /api/till/{tillId}/suspend
     * Suspend a till
     */
    @PostMapping("/{tillId}/suspend")
    public ResponseEntity<Till> suspendTill(@PathVariable String tillId) {
        try {
            Till result = service.suspendTill(tillId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}