package com.quickmart.businessday;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "business_day")
public class BusinessDay {

    @Id
    private String id;
    private String businessDayId;
    private String storeId;
    private String status;        // OPEN, CLOSED
    private String tradingDate;   // Date of trading e.g. 2026-04-15
    private double openingFloat;  // Cash at start of day
    private double closingFloat;  // Cash at end of day
    private double totalSales;    // Total sales for the day
    private double totalRefunds;  // Total refunds for the day
    private String startTime;     // When business day started
    private String endTime;       // When business day ended
    private String createdBy;     // Who opened the business day
    private String closedBy;      // Who closed the business day
}