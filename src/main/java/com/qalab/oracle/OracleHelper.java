package com.qalab.oracle;

import java.sql.*;
import java.util.*;

/**
 * OracleHelper - JDBC utility for Karate tests
 * Section 4.4 - Kafka Data Validation Queries
 *
 * In Instore project:
 * - Validates KOLOG transactions saved to Oracle
 * - Checks Kafka offset stored in DB
 * - Reconciles API amounts vs DB amounts
 * - Finds stuck transactions (not published to Kafka)
 */
public class OracleHelper {

    private final String url;
    private final String username;
    private final String password;

    public OracleHelper(String url, String username,
            String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Execute query and return results as List of Maps
     * Each Map represents one row: column name → value
     * Called from Karate feature files
     */
    public static List<Map<String, Object>> query(
            String url, String username, String password,
            String sql) {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(
                url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnName(i).toLowerCase(),
                            rs.getObject(i));
                }
                results.add(row);
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Oracle query failed: " + e.getMessage(), e);
        }
        return results;
    }

    /**
     * Get single value from query
     * Used for COUNT, SUM, MAX queries
     */
    public static Object scalar(String url, String username,
            String password, String sql) {
        try (Connection conn = DriverManager.getConnection(
                url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getObject(1);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Oracle scalar failed: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Execute DML (INSERT, UPDATE, DELETE)
     * Returns number of rows affected
     */
    public static int execute(String url, String username,
            String password, String sql) {
        try (Connection conn = DriverManager.getConnection(
                url, username, password);
             Statement stmt = conn.createStatement()) {
            int rows = stmt.executeUpdate(sql);
            conn.commit();
            return rows;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Oracle execute failed: " + e.getMessage(), e);
        }
    }

    /**
     * Check if order exists in Oracle
     * In Instore: verify KOLOG saved transaction to DB
     */
    public static boolean orderExists(String url,
            String username, String password, String orderId) {
        Object count = scalar(url, username, password,
                "SELECT COUNT(*) FROM orders WHERE order_id = '"
                + orderId + "'");
        return count != null
                && ((Number) count).intValue() > 0;
    }

    /**
     * Get order status from Oracle
     * In Instore: verify order status after Kafka processing
     */
    public static String getOrderStatus(String url,
            String username, String password, String orderId) {
        Object status = scalar(url, username, password,
                "SELECT status FROM orders WHERE order_id = '"
                + orderId + "'");
        return status != null ? status.toString() : null;
    }

    /**
     * Find orders not published to Kafka
     * In Instore: find stuck KOLOG transactions
     */
    public static List<Map<String, Object>> findUnpublishedOrders(
            String url, String username, String password) {
        return query(url, username, password,
                "SELECT order_id, status, total_amount, " +
                "created_at FROM orders " +
                "WHERE kafka_offset IS NULL " +
                "AND status = 'CREATED' " +
                "ORDER BY created_at");
    }

    /**
     * Check for duplicate orders (idempotency validation)
     * In Instore: verify no double transactions
     */
    public static List<Map<String, Object>> findDuplicateOrders(
            String url, String username, String password) {
        return query(url, username, password,
                "SELECT order_id, COUNT(*) AS row_count " +
                "FROM orders GROUP BY order_id " +
                "HAVING COUNT(*) > 1");
    }

    /**
     * Validate item totals match order total
     * In Instore: verify Kafka amount accuracy
     */
    public static List<Map<String, Object>> findAmountMismatches(
            String url, String username, String password) {
        return query(url, username, password,
                "SELECT o.order_id, o.total_amount AS order_total, " +
                "SUM(oi.total_price) AS items_sum " +
                "FROM orders o " +
                "JOIN order_items oi ON oi.order_id = o.order_id " +
                "GROUP BY o.order_id, o.total_amount " +
                "HAVING o.total_amount != SUM(oi.total_price)");
    }
}