package com.monjam.core.command;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.monjam.core.configuration.Configuration;
import com.monjam.core.rule.MongoReplicaSetRule;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

import static com.monjam.core.support.MongoUtils.find;
import static com.monjam.core.support.MongoUtils.importToCollectionFromFile;
import static com.monjam.core.support.MongoUtils.truncateCollection;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class TransactionDbRollbackIT {
    private static final String COLLECTION = "users";

    @ClassRule
    public static final MongoReplicaSetRule MONGO_RULE = new MongoReplicaSetRule();

    private static final String MESSAGES_COLLECTION = "messages";
    private static final String MIGRATIONS_COLLECTION = "schema_migrations";

    private Configuration configuration;
    private MongoDatabase database;
    private DbRollback dbRollback;

    @Before
    public void setup() throws Exception {
        configuration = new Configuration();
        configuration.setLocation("db/migration/success");
        configuration.setUrl("mongodb://localhost:27117/?replicaSet=rs0");
        configuration.setDatabase("testdb");
        configuration.setCollection(MIGRATIONS_COLLECTION);
        MongoClient client = MongoClients.create(configuration.getUrl());
        database = client.getDatabase("testdb");
        dbRollback = new DbRollback(configuration);
        importToCollectionFromFile(database, COLLECTION, "users.json");
    }

    @After
    public void teardown() {
        truncateCollection(database, MIGRATIONS_COLLECTION);
        truncateCollection(database, COLLECTION);
    }

    @Test
    public void execute_GivenAppliedMigrations() throws Exception {
        importToCollectionFromFile(database, MESSAGES_COLLECTION, "users_0_1_0.json");
        importToCollectionFromFile(database, MIGRATIONS_COLLECTION, "schema_migrations.json");

        dbRollback.execute();

        List<Document> migrations = find(database, configuration.getCollection(), Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(0));

        List<Document> documents = find(database, COLLECTION, Sorts.ascending("createdDate"));
        assertThat(documents, hasSize(2));
        assertThat(documents.get(0).getString("username"), equalTo("oun"));
        assertThat(documents.get(0).containsKey("prefix"), equalTo(false));
        assertThat(documents.get(0).getString("gender"), equalTo("M"));
        assertThat(documents.get(0).getString("nickname"), equalTo("Oun"));
        assertThat(documents.get(1).getString("username"), equalTo("palm"));
        assertThat(documents.get(1).containsKey("prefix"), equalTo(false));
        assertThat(documents.get(1).getString("gender"), equalTo("F"));
        assertThat(documents.get(1).getString("nickname"), equalTo("Palm"));
    }

    @Test
    public void execute_GivenEmptyAppliedMigrations() throws Exception {
        dbRollback.execute();

        List<Document> migrations = find(database, configuration.getCollection(), Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(0));
    }
}
