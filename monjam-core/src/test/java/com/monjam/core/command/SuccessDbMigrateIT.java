package com.monjam.core.command;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.monjam.core.api.Configuration;
import com.monjam.core.rule.MongoReplicaSetRule;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class SuccessDbMigrateIT {
    @ClassRule
    public static final MongoReplicaSetRule MONGO_RULE = new MongoReplicaSetRule();

    private Configuration configuration;
    private MongoDatabase database;
    private DbMigrate dbMigrate;

    @Before
    public void setup() {
        configuration = Configuration.builder()
                .location("db/migration/success")
                .url("mongodb://localhost:27117")
                .database("testdb")
                .collection("schema_migrations")
                .build();
        MongoClient client = MongoClients.create("mongodb://localhost:27117");
        database = client.getDatabase("testdb");
        dbMigrate = new DbMigrate(configuration);
    }

    @After
    public void teardown() {
        truncate(database, configuration.getCollection());
        database.getCollection("messages").drop();
    }

    @Test
    public void execute_GivenEmptyAppliedMigrations() throws Exception {
        dbMigrate.execute();

        List<Document> migrations = findAll(configuration.getCollection(), Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(2));
        assertThat(migrations.get(0).getString("version"), equalTo("0.1.0"));
        assertThat(migrations.get(0).getString("description"), equalTo("Create Collection"));
        assertThat(migrations.get(1).getString("version"), equalTo("0.2.0"));
        assertThat(migrations.get(1).getString("description"), equalTo("Create Index"));

        List<Document> messages = findAll("messages", Sorts.ascending("time"));
        assertThat(messages, hasSize(1));
        assertThat(messages.get(0).getString("message"), equalTo("Sawasdee Earthling"));
        assertThat(messages.get(0).getString("sender"), equalTo("Alien"));
    }

    @Test
    public void execute_GivenAppliedMigrations() throws Exception {
        insert(database, configuration.getCollection(), "schema_migrations.json");

        dbMigrate.execute();

        List<Document> migrations = findAll(configuration.getCollection(), Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(2));
        assertThat(migrations.get(0).getString("version"), equalTo("0.1.0"));
        assertThat(migrations.get(1).getString("version"), equalTo("0.2.0"));
        assertThat(migrations.get(1).getString("description"), equalTo("Create Index"));
    }

    private List<Document> findAll(String collectionName, Bson sort) {
        List<Document> documents = new ArrayList<>();
         try (MongoCursor<Document> cursor = database.getCollection(collectionName).find().sort(sort).iterator()) {
             while (cursor.hasNext()) { documents.add(cursor.next()); }
         }
         return documents;
    }

    private void insert(MongoDatabase database, String collectionName, String filePath) throws Exception {
        Path path = Paths.get(SuccessDbMigrateIT.class.getClassLoader().getResource(filePath).toURI());
        for (BsonValue value : BsonArray.parse(new String(Files.readAllBytes(path)))) {
            database.getCollection(collectionName, BsonDocument.class).insertOne(value.asDocument());
        }
    }

    private void truncate(MongoDatabase database, String collectionName) {
        database.getCollection(collectionName).deleteMany(new Document());
    }
}
