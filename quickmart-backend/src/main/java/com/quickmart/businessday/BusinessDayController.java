package com.quickmart.businessday;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/business-day")
public class BusinessDayController {

    @Autowired
    private BusinessDayService service;

    /**
     * POST /api/business-day/create
     * Create a new business day (open store for trading)
     */
    @PostMapping("/create")
    public ResponseEntity<BusinessDay> createBusinessDay(
            @RequestBody BusinessDay request) {
        try {
            BusinessDay result = service.createBusinessDay(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * POST /api/business-day/{businessDayId}/end
     * End the business day (close store)
     */
    @PostMapping("/{businessDayId}/end")
    public ResponseEntity<BusinessDay> endBusinessDay(
            @PathVariable String businessDayId,
            @RequestBody BusinessDay request) {
        try {
            BusinessDay result = service.endBusinessDay(
                    businessDayId, request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * GET /api/business-day/{businessDayId}
     * Get business day details
     */
    @GetMapping("/{businessDayId}")
    public ResponseEntity<BusinessDay> getBusinessDay(
            @PathVariable String businessDayId) {
        try {
            BusinessDay result = service.getBusinessDay(businessDayId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * GET /api/business-day/store/{storeId}/current
     * Get current open business day for a store
     */
    @GetMapping("/store/{storeId}/current")
    public ResponseEntity<BusinessDay> getCurrentBusinessDay(
            @PathVariable String storeId) {
        try {
            BusinessDay result = service.getCurrentBusinessDay(storeId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * GET /api/business-day/store/{storeId}
     * Get all business days for a store
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<BusinessDay>> getBusinessDaysByStore(
            @PathVariable String storeId) {
        return ResponseEntity.ok(
                service.getBusinessDaysByStore(storeId));
    }
}