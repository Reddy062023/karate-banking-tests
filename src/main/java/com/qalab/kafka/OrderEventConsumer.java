package com.qalab.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.time.Duration;
import java.util.*;

/**
 * OrderEventConsumer - Manual Offset Commit Consumer
 * From document Section 2.5
 *
 * In Instore project this simulates:
 * - Ingress Service consuming from Kafka
 * - Processing KOLOG transactions
 * - Manual offset commit for exactly-once processing
 */
public class OrderEventConsumer implements AutoCloseable {

    private final KafkaConsumer<String, String> consumer;
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile boolean running = true;

    public OrderEventConsumer(String groupId) {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        p.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // MANUAL commit - exactly like Instore Ingress Service
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        p.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        p.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        p.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);

        // Read only committed messages (works with idempotent producer)
        p.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        this.consumer = new KafkaConsumer<>(p);
    }

    /**
     * Consume messages from topics
     * In Instore: Ingress Service listens to cash-transactions topic
     */
    public void consume(List<String> topics) {
        consumer.subscribe(topics);
        try {
            while (running) {
                ConsumerRecords<String, String> batch =
                        consumer.poll(Duration.ofSeconds(1));

                if (batch.isEmpty()) continue;

                for (ConsumerRecord<String, String> rec : batch) {
                    try {
                        processRecord(rec);
                    } catch (Exception e) {
                        sendToDLQ(rec, e);
                    }
                }

                // Manual commit AFTER full batch processed
                // In Instore: commit only after RabbitMQ publish succeeds
                consumer.commitSync();
                System.out.println("✅ Batch committed - offset saved");
            }
        } finally {
            consumer.close();
        }
    }

    private void processRecord(ConsumerRecord<String, String> rec)
            throws Exception {

        // Read event type from header
        // In Instore: eventType determines routing to RabbitMQ queue
        String eventType = rec.headers().lastHeader("eventType") != null
                ? new String(rec.headers().lastHeader("eventType").value())
                : "UNKNOWN";

        System.out.printf("📨 [%s] partition=%d offset=%d key=%s%n",
                eventType, rec.partition(), rec.offset(), rec.key());

        JsonNode node = mapper.readTree(rec.value());

        switch (eventType) {
            case "ORDER_CREATED":
                handleCreated(
                    node.path("orderId").asText(),
                    node.path("amount").asDouble());
                break;
            case "ORDER_PAID":
                handlePaid(node.path("orderId").asText());
                break;
            case "ORDER_CANCELLED":
                handleCancelled(node.path("orderId").asText());
                break;
            default:
                System.out.println("⚠️ Unknown event: " + eventType);
        }
    }

    private void handleCreated(String id, double amt) {
        System.out.printf("🛒 ORDER_CREATED: %s $%.2f%n", id, amt);
    }

    private void handlePaid(String id) {
        System.out.printf("💳 ORDER_PAID: %s%n", id);
    }

    private void handleCancelled(String id) {
        System.out.printf("❌ ORDER_CANCELLED: %s%n", id);
    }

    /**
     * Dead Letter Queue - send failed messages for investigation
     * In Instore: failed messages go to error topic for manual review
     */
    private void sendToDLQ(ConsumerRecord<String, String> r,
            Exception e) {
        System.err.printf("→ DLQ: key=%s reason=%s%n",
                r.key(), e.getMessage());
    }

    public void stop() { running = false; }

    @Override
    public void close() { consumer.close(); }
}