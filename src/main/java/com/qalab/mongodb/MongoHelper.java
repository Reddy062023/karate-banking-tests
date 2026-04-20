package com.qalab.mongodb;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.*;

/**
 * MongoHelper - MongoDB utility for Karate tests
 * Section 5.5 - Kafka Message Validation in MongoDB
 *
 * In Instore project:
 * - Validates KOLOG events stored in MongoDB
 * - Checks Kafka metadata (topic, partition, offset)
 * - Finds DLQ events for investigation
 * - Validates event audit trail
 */
public class MongoHelper {

    /**
     * Find documents by filter
     * Returns List of Maps for Karate assertions
     */
    public static List<Map<String, Object>> find(
            String uri, String dbName, String collection,
            Map<String, Object> filter) {

        List<Map<String, Object>> results = new ArrayList<>();
        try (MongoClient client = MongoClients.create(uri)) {
            MongoCollection<Document> col = client
                    .getDatabase(dbName)
                    .getCollection(collection);

            Bson bsonFilter = filter.isEmpty()
                    ? new Document()
                    : new Document(filter);

            for (Document doc : col.find(bsonFilter)) {
                results.add(toMap(doc));
            }
        }
        return results;
    }

    /**
     * Count documents matching filter
     * In Instore: count events for specific orderId
     */
    public static long count(String uri, String dbName,
            String collection, Map<String, Object> filter) {
        try (MongoClient client = MongoClients.create(uri)) {
            MongoCollection<Document> col = client
                    .getDatabase(dbName)
                    .getCollection(collection);
            return col.countDocuments(new Document(filter));
        }
    }

    /**
     * Find event by orderId - most recent first
     * In Instore: get latest event for transaction
     */
    public static Map<String, Object> findByOrderId(
            String uri, String dbName,
            String collection, String orderId) {
        try (MongoClient client = MongoClients.create(uri)) {
            MongoCollection<Document> col = client
                    .getDatabase(dbName)
                    .getCollection(collection);

            Document doc = col.find(
                    Filters.eq("orderId", orderId))
                    .sort(Sorts.descending("timestamp"))
                    .first();

            return doc != null ? toMap(doc)
                    : new HashMap<>();
        }
    }

    /**
     * Validate Kafka metadata stored correctly
     * In Instore: verify Kafka event metadata saved to MongoDB
     */
    public static long validateKafkaMetadata(
            String uri, String dbName, String collection,
            String orderId, String topic,
            int partition, long offset) {
        try (MongoClient client = MongoClients.create(uri)) {
            MongoCollection<Document> col = client
                    .getDatabase(dbName)
                    .getCollection(collection);

            Document filter = new Document()
                    .append("orderId", orderId)
                    .append("kafka.topic", topic)
                    .append("kafka.partition", partition)
                    .append("kafka.offset", offset);

            return col.countDocuments(filter);
        }
    }

    /**
     * Find events missing Kafka metadata
     * In Instore: find events not properly published
     */
    public static List<Map<String, Object>> findMissingKafka(
            String uri, String dbName, String collection) {
        try (MongoClient client = MongoClients.create(uri)) {
            MongoCollection<Document> col = client
                    .getDatabase(dbName)
                    .getCollection(collection);

            Document filter = new Document()
                    .append("eventType", "ORDER_CREATED")
                    .append("kafka.offset",
                            new Document("$exists", false));

            List<Map<String, Object>> results = new ArrayList<>();
            for (Document doc : col.find(filter)) {
                results.add(toMap(doc));
            }
            return results;
        }
    }

    /**
     * Find DLQ events
     * In Instore: find failed transactions for investigation
     */
    public static List<Map<String, Object>> findDLQEvents(
            String uri, String dbName, String collection) {
        try (MongoClient client = MongoClients.create(uri)) {
            MongoCollection<Document> col = client
                    .getDatabase(dbName)
                    .getCollection(collection);

            Document filter = new Document()
                    .append("kafka.topic", "order-events-dlq");

            List<Map<String, Object>> results = new ArrayList<>();
            for (Document doc : col.find(filter)) {
                results.add(toMap(doc));
            }
            return results;
        }
    }

    /**
     * Get event audit trail for order
     * In Instore: see full transaction history
     */
    public static List<Map<String, Object>> getAuditTrail(
            String uri, String dbName,
            String collection, String orderId) {
        try (MongoClient client = MongoClients.create(uri)) {
            MongoCollection<Document> col = client
                    .getDatabase(dbName)
                    .getCollection(collection);

            List<Map<String, Object>> results = new ArrayList<>();
            for (Document doc : col.find(
                    Filters.eq("orderId", orderId))
                    .sort(Sorts.ascending("timestamp"))) {
                results.add(toMap(doc));
            }
            return results;
        }
    }

    private static Map<String, Object> toMap(Document doc) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : doc.entrySet()) {
            if (entry.getValue() instanceof Document) {
                map.put(entry.getKey(),
                        toMap((Document) entry.getValue()));
            } else {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }
}