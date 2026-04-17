package com.qalab.kafka;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.time.Duration;
import java.util.*;

/**
 * AvroOrderConsumer - Section 2.8
 * Reads Avro messages from Kafka using Schema Registry
 *
 * Key difference from JSON consumer:
 * - Uses KafkaAvroDeserializer instead of StringDeserializer
 * - Schema Registry auto-validates schema on read
 * - Returns GenericRecord - access fields by name
 *
 * Requires Schema Registry at localhost:8081
 */
public class AvroOrderConsumer implements AutoCloseable {

    private final KafkaConsumer<String, GenericRecord> consumer;
    private volatile boolean running = true;

    public AvroOrderConsumer(String groupId,
            String schemaRegistryUrl) {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092");
        p.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                KafkaAvroDeserializer.class);

        // Schema Registry
        p.put("schema.registry.url", schemaRegistryUrl);
        p.put("specific.avro.reader", "false"); // GenericRecord

        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest");
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false);
        p.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG,
                "read_committed");

        this.consumer = new KafkaConsumer<>(p);
    }

    /**
     * Consume Avro messages from topic
     * In Instore: validates KOLOG events use correct schema
     */
    public void consume(List<String> topics) {
        consumer.subscribe(topics);
        try {
            while (running) {
                ConsumerRecords<String, GenericRecord> batch =
                        consumer.poll(Duration.ofSeconds(1));

                if (batch.isEmpty()) continue;

                for (ConsumerRecord<String, GenericRecord>
                        rec : batch) {
                    processRecord(rec);
                }

                consumer.commitSync();
            }
        } finally {
            consumer.close();
        }
    }

    private void processRecord(
            ConsumerRecord<String, GenericRecord> rec) {

        GenericRecord avro = rec.value();

        // Read fields from Avro record
        String orderId   = String.valueOf(
                avro.get("orderId"));
        String eventType = String.valueOf(
                avro.get("eventType"));
        double amount    = (double) avro.get("amount");

        // Extract schema version from header
        var h = rec.headers().lastHeader("schemaVersion");
        String schemaVer = h != null
                ? new String(h.value()) : "unknown";

        System.out.printf(
                "📨 Avro [%s] orderId=%s amount=%.2f " +
                "schemaVer=%s partition=%d offset=%d%n",
                eventType, orderId, amount,
                schemaVer, rec.partition(), rec.offset());

        switch (eventType) {
            case "ORDER_CREATED" ->
                System.out.println("🛒 Processing: " + orderId);
            case "ORDER_PAID" ->
                System.out.println("💳 Payment: " + orderId);
            case "ORDER_CANCELLED" ->
                System.out.println("❌ Cancelled: " + orderId);
            default ->
                System.out.println("⚠️ Unknown: " + eventType);
        }
    }

    public void stop() { running = false; }

    @Override
    public void close() { consumer.close(); }
}