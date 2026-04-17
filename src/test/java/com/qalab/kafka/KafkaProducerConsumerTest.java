package com.qalab.kafka;

import org.junit.jupiter.api.Test;
import org.apache.kafka.clients.producer.RecordMetadata;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Kafka Producer and Consumer
 * Section 2.4 + 2.5
 */
public class KafkaProducerConsumerTest {

    @Test
    void testSendOrderCreated() throws Exception {
        System.out.println("\n=== TEST: Send ORDER_CREATED ===");

        try (OrderEventProducer producer = new OrderEventProducer()) {
            OrderEvent order = new OrderEvent("CUST-001", 149.99);
            order.items.add(new OrderEvent.Item("PROD-001", 2, 49.99));
            order.items.add(new OrderEvent.Item("PROD-002", 1, 50.01));

            RecordMetadata meta = producer.sendOrderCreated(order);

            assertNotNull(meta);
            assertTrue(meta.offset() >= 0);
            assertEquals(OrderEventProducer.TOPIC, meta.topic());

            System.out.println("✅ ORDER_CREATED sent successfully");
            System.out.println("   OrderId:   " + order.orderId);
            System.out.println("   Partition: " + meta.partition());
            System.out.println("   Offset:    " + meta.offset());
        }
    }

    @Test
    void testSendMultipleEvents() throws Exception {
        System.out.println("\n=== TEST: Send Multiple Events ===");

        try (OrderEventProducer producer = new OrderEventProducer()) {

            // Simulate full order lifecycle
            OrderEvent order = new OrderEvent("CUST-002", 299.99);

            // Step 1: Order created
            RecordMetadata meta1 = producer.sendOrderCreated(order);
            System.out.println("1. ORDER_CREATED offset: "
                    + meta1.offset());

            // Step 2: Order paid
            RecordMetadata meta2 = producer.sendOrderPaid(order);
            System.out.println("2. ORDER_PAID offset: "
                    + meta2.offset());

            assertNotNull(meta1);
            assertNotNull(meta2);
            assertTrue(meta2.offset() > meta1.offset());

            System.out.println("✅ Full order lifecycle sent!");
        }
    }

    @Test
    void testSendOrderCancelled() throws Exception {
        System.out.println("\n=== TEST: Send ORDER_CANCELLED (VOID) ===");

        try (OrderEventProducer producer = new OrderEventProducer()) {
            OrderEvent order = new OrderEvent("CUST-003", 75.00);

            RecordMetadata meta = producer.sendOrderCancelled(order);

            assertNotNull(meta);
            assertTrue(meta.offset() >= 0);

            System.out.println("✅ ORDER_CANCELLED sent");
            System.out.println("   This simulates VOID in Instore!");
        }
    }
}