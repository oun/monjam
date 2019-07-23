package com.monjam.core.command;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.monjam.core.api.Configuration;
import com.monjam.core.rule.MongoReplicaSetRule;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.monjam.core.support.MongoUtils.findAll;
import static com.monjam.core.support.MongoUtils.insertFile;
import static com.monjam.core.support.MongoUtils.truncate;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class TransactionDbRollbackIT {
    @ClassRule
    public static final MongoReplicaSetRule MONGO_RULE = new MongoReplicaSetRule();

    private static final String MESSAGES_COLLECTION = "messages";
    private static final String MIGRATIONS_COLLECTION = "schema_migrations";

    private Configuration configuration;
    private MongoDatabase database;
    private DbRollback dbRollback;

    @Before
    public void setup() {
        configuration = Configuration.builder()
                .location("db/migration/success")
                .url("mongodb://localhost:27117/?replicaSet=rs0")
                .database("testdb")
                .collection(MIGRATIONS_COLLECTION)
                .build();
        MongoClient client = MongoClients.create(configuration.getUrl());
        database = client.getDatabase("testdb");
        dbRollback = new DbRollback(configuration);
    }

    @After
    public void teardown() {
        truncate(database, MIGRATIONS_COLLECTION);
        database.getCollection(MESSAGES_COLLECTION).drop();
    }

    @Test
    public void execute_GivenAppliedMigrations() throws Exception {
        insertFile(database, MESSAGES_COLLECTION, "messages.json");
        insertFile(database, MIGRATIONS_COLLECTION, "schema_migrations.json");

        dbRollback.execute();

        List<Document> migrations = findAll(database, configuration.getCollection(), Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(0));

        assertThat(database.listCollectionNames().into(new ArrayList<>()), not(hasItem(MESSAGES_COLLECTION)));
    }

    @Test
    public void execute_GivenEmptyAppliedMigrations() throws Exception {
        dbRollback.execute();

        List<Document> migrations = findAll(database, configuration.getCollection(), Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(0));
    }
}
