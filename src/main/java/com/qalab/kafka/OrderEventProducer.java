package com.qalab.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.*;

/**
 * OrderEventProducer - JSON Kafka Producer
 * From document Section 2.4
 *
 * In Instore project this simulates:
 * - KOLOG sending CASH_SALE events
 * - TLOG sending transaction data
 * - Store register publishing to Kafka
 */
public class OrderEventProducer implements AutoCloseable {

    private final KafkaProducer<String, String> producer;
    private final ObjectMapper mapper = new ObjectMapper();
    public static final String TOPIC = "order-events";

    public OrderEventProducer() {
        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);

        // Reliability settings
        p.put(ProducerConfig.ACKS_CONFIG, "all"); // all ISR must ack
        p.put(ProducerConfig.RETRIES_CONFIG, 3);
        p.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        p.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);

        // Performance settings
        p.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        p.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        p.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        this.producer = new KafkaProducer<>(p);
    }

    /**
     * Send ORDER_CREATED event
     * In Instore: equivalent to sending CASH_SALE to Kafka
     */
    public RecordMetadata sendOrderCreated(OrderEvent order)
            throws Exception {
        order.eventType = "ORDER_CREATED";
        order.status = "CREATED";
        return send(order);
    }

    /**
     * Send ORDER_PAID event
     * In Instore: equivalent to payment confirmation
     */
    public RecordMetadata sendOrderPaid(OrderEvent order)
            throws Exception {
        order.eventType = "ORDER_PAID";
        order.status = "PAID";
        return send(order);
    }

    /**
     * Send ORDER_CANCELLED event
     * In Instore: equivalent to VOID transaction
     */
    public RecordMetadata sendOrderCancelled(OrderEvent order)
            throws Exception {
        order.eventType = "ORDER_CANCELLED";
        order.status = "CANCELLED";
        return send(order);
    }

    private RecordMetadata send(OrderEvent order) throws Exception {
        String key = order.customerId; // partition by customer
        String value = mapper.writeValueAsString(order);

        ProducerRecord<String, String> rec =
                new ProducerRecord<>(TOPIC, key, value);

        // Add headers - important for event routing in Instore
        rec.headers().add("eventType",
                order.eventType.getBytes());
        rec.headers().add("source",
                "order-service".getBytes());
        rec.headers().add("traceId",
                UUID.randomUUID().toString().getBytes());
        rec.headers().add("schemaVersion", "1".getBytes());

        RecordMetadata meta = producer.send(rec).get(); // synchronous

        System.out.printf("✅ Sent → topic=%s partition=%d " +
                        "offset=%d key=%s eventType=%s%n",
                TOPIC, meta.partition(), meta.offset(),
                key, order.eventType);

        return meta;
    }

    @Override
    public void close() {
        producer.flush();
        producer.close();
    }
}