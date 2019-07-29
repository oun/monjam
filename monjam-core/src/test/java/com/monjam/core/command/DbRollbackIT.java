package com.monjam.core.command;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.monjam.core.configuration.Configuration;
import com.monjam.core.rule.MongoRule;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;
import static com.monjam.core.support.MongoUtils.find;
import static com.monjam.core.support.MongoUtils.importToCollectionFromFile;
import static com.monjam.core.support.MongoUtils.insert;
import static com.monjam.core.support.MongoUtils.truncateCollection;
import static com.monjam.core.support.MongoUtils.update;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class DbRollbackIT {
    private static final String CONNECTION_URL = "mongodb://localhost:12345";
    private static final String DATABASE_NAME = "testdb";
    private static final String USERS_COLLECTION = "users";
    private static final String SCHEMA_COLLECTION = "schema_migrations";

    @ClassRule
    public static final MongoRule MONGO_RULE = new MongoRule();

    private Configuration configuration;
    private MongoDatabase database;
    private DbRollback dbRollback;

    @Before
    public void setup() throws Exception {
        configuration = new Configuration();
        configuration.setLocation("db/migration/success,db/migration/script/success");
        configuration.setUrl(CONNECTION_URL);
        configuration.setDatabase(DATABASE_NAME);
        configuration.setCollection(SCHEMA_COLLECTION);
        MongoClient client = MongoClients.create(configuration.getUrl());
        database = client.getDatabase(configuration.getDatabase());
        dbRollback = new DbRollback(configuration);
        importToCollectionFromFile(database, USERS_COLLECTION, "data/users.json");
    }

    @After
    public void teardown() {
        truncateCollection(database, configuration.getCollection());
        truncateCollection(database, USERS_COLLECTION);
    }

    @Test
    public void execute_GivenEmptyAppliedMigrations() {
        dbRollback.execute();

        List<Document> migrations = find(database, SCHEMA_COLLECTION, Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(0));

        List<Document> documents = find(database, USERS_COLLECTION, Sorts.ascending("createdDate"));
        assertThat(documents, hasSize(2));

        assertUser(documents.get(0), "oun", "Worawat", "Wijarn", null, null, "M", "Oun", 30);
        assertUser(documents.get(1), "palm", "Nattha", "Dechmontri", null, null, "F", "Palm", 20);
    }

    @Test
    public void execute_GivenAppliedMigrations() {
        insert(database, SCHEMA_COLLECTION, mockSchemaMigration("0.1.0", "Add prefix to user"));
        insert(database, SCHEMA_COLLECTION, mockSchemaMigration("0.1.1", "Add status to user"));
        update(database, USERS_COLLECTION, eq("gender", "M"), set("prefix", "Mr."));
        update(database, USERS_COLLECTION, eq("gender", "F"), set("prefix", "Mrs."));
        update(database, USERS_COLLECTION, exists("_id", true), set("status", "ACTIVE"));

        dbRollback.execute();

        List<Document> migrations = find(database, SCHEMA_COLLECTION, Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(1));
        assertSchemaMigration(migrations.get(0), "0.1.0", "Add prefix to user");

        List<Document> documents = find(database, USERS_COLLECTION, Sorts.ascending("createdDate"));
        assertThat(documents, hasSize(2));

        assertUser(documents.get(0), "oun", "Worawat", "Wijarn", "Mr.", null, "M", "Oun", 30);
        assertUser(documents.get(1), "palm", "Nattha", "Dechmontri", "Mrs.", null, "F", "Palm", 20);
    }

    @Test
    public void execute_WithoutRecentRollbackMigration() {
        insert(database, SCHEMA_COLLECTION, mockSchemaMigration("0.1.0", "Add prefix to user"));
        insert(database, SCHEMA_COLLECTION, mockSchemaMigration("0.1.1", "Add status to user"));
        insert(database, SCHEMA_COLLECTION, mockSchemaMigration("0.2.0", "Remove nickname from user"));
        insert(database, SCHEMA_COLLECTION, mockSchemaMigration("0.2.1", "Remove age from user"));
        update(database, USERS_COLLECTION, eq("gender", "M"), set("prefix", "Mr."));
        update(database, USERS_COLLECTION, eq("gender", "F"), set("prefix", "Mrs."));
        update(database, USERS_COLLECTION, exists("_id", true), set("status", "ACTIVE"));
        update(database, USERS_COLLECTION, exists("nickname", true), unset("nickname"));
        update(database, USERS_COLLECTION, exists("age", true), unset("age"));
        // No rollback migration for version 0.2.1

        dbRollback.execute();

        List<Document> migrations = find(database, SCHEMA_COLLECTION, Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(3));
        assertSchemaMigration(migrations.get(0), "0.1.0", "Add prefix to user");
        assertSchemaMigration(migrations.get(1), "0.1.1", "Add status to user");
        assertSchemaMigration(migrations.get(2), "0.2.0", "Remove nickname from user");

        List<Document> documents = find(database, USERS_COLLECTION, Sorts.ascending("createdDate"));
        assertThat(documents, hasSize(2));

        // user age is still removed
        assertUser(documents.get(0), "oun", "Worawat", "Wijarn", "Mr.", "ACTIVE", "M", null, null);
        assertUser(documents.get(1), "palm", "Nattha", "Dechmontri", "Mrs.", "ACTIVE", "F", null, null);
    }

    private void assertSchemaMigration(Document migration, String version, String description) {
        assertThat(migration.getString("version"), equalTo(version));
        assertThat(migration.getString("description"), equalTo(description));
    }

    private void assertUser(Document user, String username, String firstName, String lastName, String prefix, String status, String gender, String nickname, Integer age) {
        assertThat(user.getString("username"), equalTo(username));
        assertThat(user.getString("firstName"), equalTo(firstName));
        assertThat(user.getString("lastName"), equalTo(lastName));
        assertThat(user.getString("prefix"), equalTo(prefix));
        assertThat(user.getString("status"), equalTo(status));
        assertThat(user.getString("gender"), equalTo(gender));
        assertThat(user.getString("nickname"), equalTo(nickname));
        assertThat(user.getInteger("age"), equalTo(age));
    }

    private Document mockSchemaMigration(String version, String description) {
        return new Document().append("version", version).append("description", description).append("executedAt", ZonedDateTime.now().toInstant());
    }
}
