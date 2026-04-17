package com.qalab.kafka;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.*;

/**
 * AvroOrderProducer - Section 2.8
 * Uses Schema Registry to validate and store schema
 * In Instore: enterprise Kafka uses Avro for type safety
 *
 * Requires Schema Registry running at localhost:8081
 * Start with: docker compose up -d schema-registry
 */
public class AvroOrderProducer implements AutoCloseable {

    private final KafkaProducer<String, GenericRecord> producer;
    private final Schema schema;
    public static final String TOPIC = "order-events-avro";

    public AvroOrderProducer(String schemaRegistryUrl) {
        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092");
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                KafkaAvroSerializer.class);

        // Schema Registry URL
        p.put("schema.registry.url", schemaRegistryUrl);

        // Reliability
        p.put(ProducerConfig.ACKS_CONFIG, "all");
        p.put(ProducerConfig.RETRIES_CONFIG, 3);
        p.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        this.producer = new KafkaProducer<>(p);

        // Load schema from file
        this.schema = loadSchema();
    }

    /**
     * Send ORDER_CREATED event using Avro schema
     * Schema Registry validates the schema before sending
     */
    public RecordMetadata sendOrderEvent(
            String orderId, String customerId,
            double amount, String eventType) throws Exception {

        // Create Avro GenericRecord
        GenericRecord record = new GenericData.Record(schema);
        record.put("orderId",    orderId);
        record.put("customerId", customerId);
        record.put("amount",     amount);
        record.put("currency",   "USD");
        record.put("status",     "CREATED");
        record.put("eventType",  eventType);
        record.put("timestamp",  System.currentTimeMillis());
        record.put("items",      new ArrayList<>());

        ProducerRecord<String, GenericRecord> rec =
                new ProducerRecord<>(TOPIC, customerId, record);

        // Add headers
        rec.headers().add("eventType",
                eventType.getBytes());
        rec.headers().add("schemaVersion", "1".getBytes());
        rec.headers().add("source",
                "avro-producer".getBytes());

        RecordMetadata meta = producer.send(rec).get();

        System.out.printf(
                "✅ Avro Sent → topic=%s partition=%d " +
                "offset=%d orderId=%s%n",
                TOPIC, meta.partition(),
                meta.offset(), orderId);

        return meta;
    }

    private Schema loadSchema() {
        try {
            // Load from classpath
            var stream = getClass().getClassLoader()
                    .getResourceAsStream("avro/order-event.avsc");
            if (stream != null) {
                return new Schema.Parser().parse(stream);
            }
            // Fallback - inline schema
            return new Schema.Parser().parse(
                    getInlineSchema());
        } catch (Exception e) {
            return new Schema.Parser().parse(
                    getInlineSchema());
        }
    }

    private String getInlineSchema() {
        return """
            {
              "type": "record",
              "name": "OrderEvent",
              "namespace": "com.qalab.avro",
              "fields": [
                {"name": "orderId",    "type": "string"},
                {"name": "customerId", "type": "string"},
                {"name": "amount",     "type": "double"},
                {"name": "currency",   "type": "string",
                 "default": "USD"},
                {"name": "status",     "type": "string"},
                {"name": "eventType",  "type": "string"},
                {"name": "timestamp",  "type": "long"},
                {"name": "items",
                 "type": {"type": "array",
                   "items": {"type": "record",
                     "name": "OrderItem",
                     "fields": [
                       {"name": "productId", "type": "string"},
                       {"name": "qty",       "type": "int"},
                       {"name": "price",     "type": "double"}
                     ]}},
                 "default": []}
              ]
            }
            """;
    }

    @Override
    public void close() {
        producer.flush();
        producer.close();
    }
}