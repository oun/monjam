package com.monjam.core.command;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.monjam.core.api.Configuration;
import com.monjam.core.rule.MongoRule;
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

public class DbMigrateIT {
    @ClassRule
    public static final MongoRule MONGO_RULE = new MongoRule();

    private Configuration configuration;
    private MongoDatabase database;
    private DbMigrate dbMigrate;

    @Before
    public void setup() {
        configuration = Configuration.builder()
                .location("com.monjam.core.db.migration")
                .url("mongodb://localhost:12345")
                .database("testdb")
                .build();
        MongoClient client = MongoClients.create("mongodb://localhost:12345");
        database = client.getDatabase("testdb");
        dbMigrate = new DbMigrate(configuration);
    }

    @After
    public void teardown() {
        database.getCollection(configuration.getCollection()).drop();
    }

    @Test
    public void execute_GivenEmptyAppliedMigrations() throws Exception {
        dbMigrate.execute();

        List<Document> migrations = findAll(configuration.getCollection(), Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(2));
        assertThat(migrations.get(0).getString("version"), equalTo("0.1.0"));
        assertThat(migrations.get(0).getString("description"), equalTo("Create collection"));
        assertThat(migrations.get(1).getString("version"), equalTo("0.1.1"));
        assertThat(migrations.get(1).getString("description"), equalTo("Create index"));

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
        assertThat(migrations.get(1).getString("version"), equalTo("0.1.1"));
        assertThat(migrations.get(1).getString("description"), equalTo("Create index"));
    }

    private List<Document> findAll(String collectionName, Bson sort) {
        List<Document> documents = new ArrayList<>();
         try (MongoCursor<Document> cursor = database.getCollection(collectionName).find().sort(sort).iterator()) {
             while (cursor.hasNext()) { documents.add(cursor.next()); }
         }
         return documents;
    }

    private void insert(MongoDatabase database, String collectionName, String filePath) throws Exception {
        Path path = Paths.get(DbMigrateIT.class.getClassLoader().getResource(filePath).toURI());
        for (BsonValue value : BsonArray.parse(new String(Files.readAllBytes(path)))) {
            database.getCollection(collectionName, BsonDocument.class).insertOne(value.asDocument());
        }
    }
}
