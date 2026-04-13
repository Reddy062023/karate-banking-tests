package com.qalab.helpers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDBHelper {

    private final MongoClient mongoClient;
    private final MongoDatabase database;

    public MongoDBHelper(String mongoUri, String databaseName) {
        this.mongoClient = MongoClients.create(mongoUri);
        this.database = mongoClient.getDatabase(databaseName);
    }

    // ── Find one document by field ──────────────────────────────
    public Document findOne(String collection, String field, String value) {
        MongoCollection<Document> col = database.getCollection(collection);
        return col.find(new Document(field, value)).first();
    }

    // ── Find all documents in collection ───────────────────────
    public List<Document> findAll(String collection) {
        MongoCollection<Document> col = database.getCollection(collection);
        List<Document> results = new ArrayList<>();
        col.find().into(results);
        return results;
    }

    // ── Insert a document ───────────────────────────────────────
    public void insertOne(String collection, Document document) {
        MongoCollection<Document> col = database.getCollection(collection);
        col.insertOne(document);
        System.out.println("Document inserted into: " + collection);
    }

    // ── Delete a document by field ──────────────────────────────
    public void deleteOne(String collection, String field, String value) {
        MongoCollection<Document> col = database.getCollection(collection);
        col.deleteOne(new Document(field, value));
        System.out.println("Document deleted from: " + collection);
    }

    // ── Count documents in collection ───────────────────────────
    public long countDocuments(String collection) {
        MongoCollection<Document> col = database.getCollection(collection);
        return col.countDocuments();
    }

    // ── Close connection ────────────────────────────────────────
    public void close() {
        mongoClient.close();
    }
}