package com.qalab.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * OrderEvent - simulates real retail order event
 * In Instore project this maps to KOLOG/TLOG transaction data
 */
public class OrderEvent {

    public String orderId;
    public String customerId;
    public String currency = "USD";
    public String status = "CREATED";
    public String eventType = "ORDER_CREATED";
    public double amount;
    public long timestamp = System.currentTimeMillis();
    public List<Item> items = new ArrayList<>();

    // Default constructor for Jackson
    public OrderEvent() {}

    // Convenience constructor
    public OrderEvent(String customerId, double amount) {
        this.orderId = "ORD-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
        this.customerId = customerId;
        this.amount = amount;
    }

    public static class Item {
        public String productId;
        public int qty;
        public double price;

        public Item() {}

        public Item(String productId, int qty, double price) {
            this.productId = productId;
            this.qty = qty;
            this.price = price;
        }
    }

    @Override
    public String toString() {
        return String.format("OrderEvent{orderId=%s, customerId=%s, " +
                "amount=%.2f, status=%s}", orderId, customerId,
                amount, status);
    }
}