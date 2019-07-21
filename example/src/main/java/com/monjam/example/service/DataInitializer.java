package com.monjam.example.service;

import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Profile("data")
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) throws Exception {
        loadFromFile(mongoTemplate.getDb(), "users", "data/sample_users.json");
    }

    public void loadFromFile(MongoDatabase mongoDatabase, String collectionName, String filePath) throws Exception {
        Path path = Paths.get(new ClassPathResource(filePath).getURI());
        BsonArray documents = BsonArray.parse(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
        for (BsonValue value : documents) {
            mongoDatabase.getCollection(collectionName, BsonDocument.class).insertOne(value.asDocument());
        }
    }
}
