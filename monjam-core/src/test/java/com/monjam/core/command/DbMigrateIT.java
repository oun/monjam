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

public class DbMigrateIT {
    private static final String CONNECTION_URL = "mongodb://localhost:12345";
    private static final String DATABASE_NAME = "testdb";
    private static final String USERS_COLLECTION = "users";
    private static final String MESSAGES_COLLECTION = "messages";
    private static final String SCHEMA_COLLECTION = "schema_migrations";

    @ClassRule
    public static final MongoRule MONGO_RULE = new MongoRule();

    private Configuration configuration;
    private MongoDatabase database;
    private DbMigrate dbMigrate;

    @Before
    public void setup() throws Exception {
        configuration = new Configuration();
        configuration.setLocation("db/migration/success,db/migration/annotation,db/migration/script/success");
        configuration.setUrl(CONNECTION_URL);
        configuration.setDatabase(DATABASE_NAME);
        configuration.setCollection(SCHEMA_COLLECTION);
        MongoClient client = MongoClients.create(configuration.getUrl());
        database = client.getDatabase(configuration.getDatabase());
        dbMigrate = new DbMigrate(configuration);
        importToCollectionFromFile(database, USERS_COLLECTION, "data/users.json");
        importToCollectionFromFile(database, MESSAGES_COLLECTION, "data/messages.json");
    }

    @After
    public void teardown() {
        truncateCollection(database, SCHEMA_COLLECTION);
        truncateCollection(database, USERS_COLLECTION);
        truncateCollection(database, MESSAGES_COLLECTION);
    }

    @Test
    public void execute_GivenEmptyAppliedMigrations() {
        dbMigrate.execute();

        List<Document> migrations = find(database, SCHEMA_COLLECTION, Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(9));
        assertSchemaMigration(migrations.get(0), "0.1.0", "Add prefix to user");
        assertSchemaMigration(migrations.get(1), "0.1.1", "Add status to user");
        assertSchemaMigration(migrations.get(2), "0.1.2", "Add marital status");
        assertSchemaMigration(migrations.get(3), "0.2.0", "Remove nickname from user");
        assertSchemaMigration(migrations.get(4), "0.2.1", "Remove age from user");
        assertSchemaMigration(migrations.get(5), "0.2.2", "Add username index");
        assertSchemaMigration(migrations.get(6), "0.3.0", "Update user gender");
        assertSchemaMigration(migrations.get(7), "0.3.1", "Update user last name");
        assertSchemaMigration(migrations.get(8), "0.3.2", "Add read flag to message");

        List<Document> documents = find(database, USERS_COLLECTION, Sorts.ascending("createdDate"));
        assertThat(documents, hasSize(2));

        assertUser(documents.get(0), "oun", "Worawat", "Wijarn", "Mr.", "ACTIVE", 1, null, null, "M");
        assertUser(documents.get(1), "palm", "Nattha", "Wijarn", "Mrs.", "ACTIVE", 2, null, null, "M");

        List<Document> messages = find(database, MESSAGES_COLLECTION, Sorts.ascending("createdDate"));
        assertMessage(messages.get(0), "Hello. How are you?", "Nattha Wijarn", null, true);
    }

    @Test
    public void execute_GivenAppliedMigrations() {
        insert(database, SCHEMA_COLLECTION, mockSchemaMigration("0.1.0", "Add prefix to user"));
        insert(database, SCHEMA_COLLECTION, mockSchemaMigration("0.1.1", "Add status to user"));
        update(database, USERS_COLLECTION, eq("gender", "M"), set("prefix", "Mr."));
        update(database, USERS_COLLECTION, eq("gender", "F"), set("prefix", "Mrs."));
        update(database, USERS_COLLECTION, exists("_id", true), set("status", "ACTIVE"));

        dbMigrate.execute();

        List<Document> migrations = find(database, SCHEMA_COLLECTION, Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(9));
        assertSchemaMigration(migrations.get(0), "0.1.0", "Add prefix to user");
        assertSchemaMigration(migrations.get(1), "0.1.1", "Add status to user");
        assertSchemaMigration(migrations.get(2), "0.1.2", "Add marital status");
        assertSchemaMigration(migrations.get(3), "0.2.0", "Remove nickname from user");
        assertSchemaMigration(migrations.get(4), "0.2.1", "Remove age from user");
        assertSchemaMigration(migrations.get(5), "0.2.2", "Add username index");
        assertSchemaMigration(migrations.get(6), "0.3.0", "Update user gender");
        assertSchemaMigration(migrations.get(7), "0.3.1", "Update user last name");
        assertSchemaMigration(migrations.get(8), "0.3.2", "Add read flag to message");

        List<Document> documents = find(database, USERS_COLLECTION, Sorts.ascending("createdDate"));
        assertThat(documents, hasSize(2));

        assertUser(documents.get(0), "oun", "Worawat", "Wijarn", "Mr.", "ACTIVE", 1, null, null, "M");
        assertUser(documents.get(1), "palm", "Nattha", "Wijarn", "Mrs.", "ACTIVE", 2, null, null, "M");

        List<Document> messages = find(database, MESSAGES_COLLECTION, Sorts.ascending("createdDate"));
        assertMessage(messages.get(0), "Hello. How are you?", "Nattha Wijarn", null, true);
    }

    @Test
    public void execute_GivenSkipMigrations() {
        insert(database, SCHEMA_COLLECTION, mockSchemaMigration("0.1.0", "Add prefix to user"));
        insert(database, SCHEMA_COLLECTION, mockSchemaMigration("0.2.1", "Remove age from user"));
        update(database, USERS_COLLECTION, eq("gender", "M"), set("prefix", "Mr."));
        update(database, USERS_COLLECTION, eq("gender", "F"), set("prefix", "Mrs."));
        update(database, USERS_COLLECTION, exists("_id", true), unset("age"));
        update(database, USERS_COLLECTION, exists("_id", true), set("maritalStatus", "M"));

        dbMigrate.execute();

        List<Document> migrations = find(database, SCHEMA_COLLECTION, Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(6));
        assertSchemaMigration(migrations.get(0), "0.1.0", "Add prefix to user");
        assertSchemaMigration(migrations.get(1), "0.2.1", "Remove age from user");
        assertSchemaMigration(migrations.get(2), "0.2.2", "Add username index");
        assertSchemaMigration(migrations.get(3), "0.3.0", "Update user gender");
        assertSchemaMigration(migrations.get(4), "0.3.1", "Update user last name");
        assertSchemaMigration(migrations.get(5), "0.3.2", "Add read flag to message");

        List<Document> documents = find(database, USERS_COLLECTION, Sorts.ascending("createdDate"));
        assertThat(documents, hasSize(2));

        assertUser(documents.get(0), "oun", "Worawat", "Wijarn", "Mr.", null, 1, "Oun", null, "M");
        assertUser(documents.get(1), "palm", "Nattha", "Wijarn", "Mrs.", null, 2, "Palm", null, "M");
    }

    @Test
    public void execute_GivenMigrationError() {
        configuration = new Configuration();
        configuration.setLocation("db/migration/success,db/migration/failure,db/migration/script/success");
        configuration.setUrl(CONNECTION_URL);
        configuration.setDatabase(DATABASE_NAME);
        configuration.setCollection(SCHEMA_COLLECTION);
        MongoClient client = MongoClients.create(configuration.getUrl());
        database = client.getDatabase(configuration.getDatabase());
        dbMigrate = new DbMigrate(configuration);

        dbMigrate.execute();

        List<Document> migrations = find(database, SCHEMA_COLLECTION, Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(2));
        assertSchemaMigration(migrations.get(0), "0.1.0", "Add prefix to user");
        assertSchemaMigration(migrations.get(1), "0.1.1", "Add status to user");

        List<Document> documents = find(database, USERS_COLLECTION, Sorts.ascending("createdDate"));
        assertThat(documents, hasSize(2));

        assertUser(documents.get(0), "oun", "Worawat", "Wijarn", "Mr.", "ACTIVE", "M", "Oun", 30, null);
        assertUser(documents.get(1), "palm", "Nattha", "Dechmontri", "Mrs.", "ACTIVE", "F", "Palm", 20, null);
    }

    @Test
    public void execute_GivenTargetVersionLessThanLatestVersion() {
        configuration.setTarget("0.2.0");
        dbMigrate = new DbMigrate(configuration);

        dbMigrate.execute();

        List<Document> migrations = find(database, SCHEMA_COLLECTION, Sorts.ascending("executedAt"));
        assertThat(migrations, hasSize(4));
        assertSchemaMigration(migrations.get(0), "0.1.0", "Add prefix to user");
        assertSchemaMigration(migrations.get(1), "0.1.1", "Add status to user");
        assertSchemaMigration(migrations.get(2), "0.1.2", "Add marital status");
        assertSchemaMigration(migrations.get(3), "0.2.0", "Remove nickname from user");

        List<Document> documents = find(database, USERS_COLLECTION, Sorts.ascending("createdDate"));
        assertThat(documents, hasSize(2));

        assertUser(documents.get(0), "oun", "Worawat", "Wijarn", "Mr.", "ACTIVE", "M", null, 30, "M");
        assertUser(documents.get(1), "palm", "Nattha", "Dechmontri", "Mrs.", "ACTIVE", "F", null, 20, "M");
    }

    private void assertSchemaMigration(Document migration, String version, String description) {
        assertThat(migration.getString("version"), equalTo(version));
        assertThat(migration.getString("description"), equalTo(description));
    }

    private void assertUser(Document user, String username, String firstName, String lastName, String prefix, String status, Object gender, String nickname, Integer age, String maritalStatus) {
        assertThat(user.getString("username"), equalTo(username));
        assertThat(user.getString("firstName"), equalTo(firstName));
        assertThat(user.getString("lastName"), equalTo(lastName));
        assertThat(user.getString("prefix"), equalTo(prefix));
        assertThat(user.getString("status"), equalTo(status));
        assertThat(user.get("gender"), equalTo(gender));
        assertThat(user.getString("nickname"), equalTo(nickname));
        assertThat(user.getInteger("age"), equalTo(age));
        assertThat(user.getString("maritalStatus"), equalTo(maritalStatus));
    }

    private void assertMessage(Document document, String message, String name, String nickname, boolean read) {
        assertThat(document.getString("message"), equalTo(message));
        assertThat(document.getString("name"), equalTo(name));
        assertThat(document.getString("nickname"), equalTo(nickname));
        assertThat(document.getBoolean("read"), equalTo(read));
    }

    private Document mockSchemaMigration(String version, String description) {
        return new Document().append("version", version).append("description", description).append("executedAt", ZonedDateTime.now().toInstant());
    }
}
