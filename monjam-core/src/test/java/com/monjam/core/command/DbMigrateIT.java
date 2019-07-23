package com.monjam.core.command;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.monjam.core.api.Configuration;
import com.monjam.core.rule.MongoRule;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

import static com.monjam.core.support.MongoUtils.findAll;
import static com.monjam.core.support.MongoUtils.insertFile;
import static com.monjam.core.support.MongoUtils.truncate;
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
                .location("db/migration/success")
                .url("mongodb://localhost:12345")
                .database("testdb")
                .collection("schema_migrations")
                .build();
        MongoClient client = MongoClients.create(configuration.getUrl());
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

        List<Document> migrations = findAll(database, configuration.getCollection(), Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(2));
        assertThat(migrations.get(0).getString("version"), equalTo("0.1.0"));
        assertThat(migrations.get(0).getString("description"), equalTo("Create Collection"));
        assertThat(migrations.get(1).getString("version"), equalTo("0.2.0"));
        assertThat(migrations.get(1).getString("description"), equalTo("Create Index"));

        List<Document> messages = findAll(database,"messages", Sorts.ascending("time"));
        assertThat(messages, hasSize(1));
        assertThat(messages.get(0).getString("message"), equalTo("Sawasdee Earthling"));
        assertThat(messages.get(0).getString("sender"), equalTo("Alien"));
    }

    @Test
    public void execute_GivenAppliedMigrations() throws Exception {
        insertFile(database, configuration.getCollection(), "schema_migrations.json");

        dbMigrate.execute();

        List<Document> migrations = findAll(database, configuration.getCollection(), Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(2));
        assertThat(migrations.get(0).getString("version"), equalTo("0.1.0"));
        assertThat(migrations.get(1).getString("version"), equalTo("0.2.0"));
        assertThat(migrations.get(1).getString("description"), equalTo("Create Index"));
    }
}
