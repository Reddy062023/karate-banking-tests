package com.qalab.helpers;

import com.fasterxml.jackson.databind.*;
import org.apache.kafka.clients.consumer.*;
import java.time.*;
import java.util.*;

public class KafkaTestHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Map poll(String broker, String topic,
            String orderId, int secs) {
        try (KafkaConsumer<String, String> c =
                new KafkaConsumer<>(props(broker))) {
            c.subscribe(List.of(topic));
            Instant end = Instant.now().plusSeconds(secs);
            while (Instant.now().isBefore(end)) {
                for (ConsumerRecord<String, String> r :
                        c.poll(Duration.ofMillis(500))) {
                    JsonNode n = MAPPER.readTree(r.value());
                    if (orderId.equals(
                            n.path("orderId").asText())) {
                        return toMap(r, n);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        throw new AssertionError(
                "Not found: orderId=" + orderId);
    }

    public static Map pollByType(String broker, String topic,
            String eventType, int secs) {
        try (KafkaConsumer<String, String> c =
                new KafkaConsumer<>(props(broker))) {
            c.subscribe(List.of(topic));
            Instant end = Instant.now().plusSeconds(secs);
            while (Instant.now().isBefore(end)) {
                for (ConsumerRecord<String, String> r :
                        c.poll(Duration.ofMillis(500))) {
                    var h = r.headers()
                            .lastHeader("eventType");
                    String ht = h != null
                            ? new String(h.value()) : "";
                    if (eventType.equals(ht)) {
                        JsonNode n = MAPPER.readTree(r.value());
                        return toMap(r, n);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        throw new AssertionError(
                "Not found: eventType=" + eventType);
    }

    private static Map toMap(
            ConsumerRecord<String, String> r, JsonNode n) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("orderId",    n.path("orderId").asText());
        m.put("customerId", n.path("customerId").asText());
        m.put("amount",     n.path("amount").asDouble());
        m.put("currency",   n.path("currency").asText("USD"));
        m.put("status",     n.path("status").asText());
        m.put("eventType",  n.path("eventType").asText());
        m.put("timestamp",  n.path("timestamp").asLong());
        m.put("partition",  r.partition());
        m.put("offset",     r.offset());
        m.put("key",        r.key());
        m.put("topic",      r.topic());
        var h = r.headers().lastHeader("eventType");
        if (h != null)
            m.put("headerEventType", new String(h.value()));
        return m;
    }

    private static Properties props(String broker) {
        Properties p = new Properties();
        p.put("bootstrap.servers", broker);
        p.put("group.id", "kt-" + UUID.randomUUID());
        p.put("key.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer");
        p.put("value.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer");
        p.put("auto.offset.reset", "earliest");
        p.put("enable.auto.commit", "false");
        return p;
    }
}