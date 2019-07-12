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

import java.util.List;

import static com.monjam.core.support.MongoUtils.findAll;
import static com.monjam.core.support.MongoUtils.truncate;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class FailureDbMigrateIT {
    @ClassRule
    public static final MongoReplicaSetRule MONGO_RULE = new MongoReplicaSetRule();

    private Configuration configuration;
    private MongoDatabase database;
    private DbMigrate dbMigrate;

    @Before
    public void setup() {
        configuration = Configuration.builder()
                .location("db/migration/success,db/migration/failure")
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
    public void execute_GivenMigrationThrowError() {
        dbMigrate.execute();

        List<Document> migrations = findAll(database, configuration.getCollection(), Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(1));
        assertThat(migrations.get(0).getString("version"), equalTo("0.1.0"));
        assertThat(migrations.get(0).getString("description"), equalTo("Create Collection"));

        Document document = database.getCollection("messages").find().first();
        assertThat(document, is(notNullValue()));
        assertThat(document.getString("subject"), is(nullValue()));
        assertThat(document.getString("message"), equalTo("Sawasdee Earthling"));
        assertThat(document.getString("sender"), equalTo("Alien"));
    }
}
