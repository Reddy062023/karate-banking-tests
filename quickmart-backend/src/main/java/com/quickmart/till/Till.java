package com.quickmart.till;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "till")
public class Till {

    @Id
    private String id;
    private String tillId;
    private String storeId;
    private String cashierId;
    private String status;         // OPEN, CLOSED, SUSPENDED
    private double openingBalance;
    private double closingBalance;
    private double currentBalance;
    private String openedAt;
    private String closedAt;
    private String lastUpdated;
}