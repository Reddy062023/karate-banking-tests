package com.quickmart.cash;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "cash_transactions")
public class CashTransaction {

    @Id
    private String id;
    private String transactionId;
    private String storeId;
    private String cashierId;
    private double amount;
    private String type;      // INBOUND, OUTBOUND, LOAN, PICKUP
    private String reason;    // CASH_SALE, REFUND, CASH_LOAN, CASH_PICKUP
    private String currency;
    private String status;    // PENDING, PROCESSED
    private String timestamp;
    private String processedAt;
}