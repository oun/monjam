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

public class TransactionDbMigrateIT {
    private static final String COLLECTION = "users";

    @ClassRule
    public static final MongoReplicaSetRule MONGO_RULE = new MongoReplicaSetRule();

    private Configuration configuration;
    private MongoDatabase database;
    private DbMigrate dbMigrate;

    @Before
    public void setup() throws Exception {
        configuration = new Configuration();
        configuration.setLocation("db/migration/success");
        configuration.setUrl("mongodb://localhost:27117/?replicaSet=rs0");
        configuration.setDatabase("testdb");
        configuration.setCollection("schema_migrations");
        MongoClient client = MongoClients.create(configuration.getUrl());
        database = client.getDatabase("testdb");
        dbMigrate = new DbMigrate(configuration);
        importToCollectionFromFile(database, COLLECTION, "users.json");
    }

    @After
    public void teardown() {
        truncateCollection(database, configuration.getCollection());
        truncateCollection(database, COLLECTION);
    }

    @Test
    public void execute_GivenEmptyAppliedMigrations() throws Exception {
        dbMigrate.execute();

        List<Document> migrations = find(database, configuration.getCollection(), Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(3));
        assertThat(migrations.get(0).getString("version"), equalTo("0.1.0"));
        assertThat(migrations.get(0).getString("description"), equalTo("Add prefix to user"));
        assertThat(migrations.get(1).getString("version"), equalTo("0.2.0"));
        assertThat(migrations.get(1).getString("description"), equalTo("Remove nickname from user"));
        assertThat(migrations.get(2).getString("version"), equalTo("0.3.0"));
        assertThat(migrations.get(2).getString("description"), equalTo("Update user gender"));

        List<Document> documents = find(database, COLLECTION, Sorts.ascending("createdDate"));
        assertThat(documents, hasSize(2));
        assertThat(documents.get(0).getString("username"), equalTo("oun"));
        assertThat(documents.get(0).getString("prefix"), equalTo("Mr."));
        assertThat(documents.get(0).getInteger("gender"), equalTo(1));
        assertThat(documents.get(0).containsKey("nickname"), equalTo(false));
        assertThat(documents.get(1).getString("username"), equalTo("palm"));
        assertThat(documents.get(1).getString("prefix"), equalTo("Mrs."));
        assertThat(documents.get(1).getInteger("gender"), equalTo(2));
        assertThat(documents.get(1).containsKey("nickname"), equalTo(false));
    }

    @Test
    public void execute_GivenAppliedMigrations() throws Exception {
        importToCollectionFromFile(database, configuration.getCollection(), "schema_migrations.json");

        dbMigrate.execute();

        List<Document> migrations = find(database, configuration.getCollection(), Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(3));
        assertThat(migrations.get(0).getString("version"), equalTo("0.1.0"));
        assertThat(migrations.get(0).getString("description"), equalTo("Add prefix to user"));
        assertThat(migrations.get(1).getString("version"), equalTo("0.2.0"));
        assertThat(migrations.get(1).getString("description"), equalTo("Remove nickname from user"));
        assertThat(migrations.get(2).getString("version"), equalTo("0.3.0"));
        assertThat(migrations.get(2).getString("description"), equalTo("Update user gender"));

        List<Document> documents = find(database, COLLECTION, Sorts.ascending("createdDate"));
        assertThat(documents, hasSize(2));
        assertThat(documents.get(0).getString("username"), equalTo("oun"));
        assertThat(documents.get(0).containsKey("prefix"), equalTo(false));
        assertThat(documents.get(0).getInteger("gender"), equalTo(1));
        assertThat(documents.get(0).containsKey("nickname"), equalTo(false));
        assertThat(documents.get(1).getString("username"), equalTo("palm"));
        assertThat(documents.get(1).containsKey("prefix"), equalTo(false));
        assertThat(documents.get(1).getInteger("gender"), equalTo(2));
        assertThat(documents.get(1).containsKey("nickname"), equalTo(false));
    }

    @Test
    public void execute_GivenMigrationThrowError() {
        configuration = new Configuration();
        configuration.setLocation("db/migration/success,db/migration/failure");
        configuration.setUrl("mongodb://localhost:27117/?replicaSet=rs0");
        configuration.setDatabase("testdb");
        configuration.setCollection("schema_migrations");
        MongoClient client = MongoClients.create(configuration.getUrl());
        database = client.getDatabase("testdb");
        dbMigrate = new DbMigrate(configuration);

        dbMigrate.execute();

        List<Document> migrations = find(database, configuration.getCollection(), Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(1));
        assertThat(migrations.get(0).getString("version"), equalTo("0.1.0"));
        assertThat(migrations.get(0).getString("description"), equalTo("Add prefix to user"));

        List<Document> documents = find(database, COLLECTION, Sorts.ascending("createdDate"));
        assertThat(documents, hasSize(2));
        assertThat(documents.get(0).getString("username"), equalTo("oun"));
        assertThat(documents.get(0).getString("prefix"), equalTo("Mr."));
        assertThat(documents.get(0).getString("gender"), equalTo("M"));
        assertThat(documents.get(0).getString("nickname"), equalTo("Oun"));
        assertThat(documents.get(1).getString("username"), equalTo("palm"));
        assertThat(documents.get(1).getString("prefix"), equalTo("Mrs."));
        assertThat(documents.get(1).getString("gender"), equalTo("F"));
        assertThat(documents.get(1).getString("nickname"), equalTo("Palm"));
    }
}
