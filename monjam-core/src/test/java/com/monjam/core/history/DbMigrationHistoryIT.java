package com.monjam.core.history;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.monjam.core.api.Configuration;
import com.monjam.core.api.MigrationVersion;
import com.monjam.core.database.MongoTemplate;
import com.monjam.core.rule.MongoReplicaSetRule;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.List;

import static com.monjam.core.support.MongoUtils.findAll;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class DbMigrationHistoryIT {
    private static final String SCHEMA_MIGRATIONS_COLLECTION = "schema_migrations";
    @ClassRule
    public static final MongoReplicaSetRule MONGO_RULE = new MongoReplicaSetRule();
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private DbMigrationHistory migrationHistory;
    private MongoDatabase database;

    @Before
    public void setup() {
        Configuration configuration = new Configuration();
        MongoClient client = MongoClients.create("mongodb://localhost:27117");
        ClientSession session = client.startSession();
        MongoTemplate mongoTemplate = new MongoTemplate(client, session, "testdb");
        database = client.getDatabase("testdb");
        database.createCollection(SCHEMA_MIGRATIONS_COLLECTION);
        migrationHistory = new DbMigrationHistory(mongoTemplate, configuration);
    }

    @After
    public void teardown() {
        database.getCollection(SCHEMA_MIGRATIONS_COLLECTION).drop();
    }

    @Test
    public void getAppliedMigrations_GivenMigrationExist() {
        createDocument("0.1.0", "Hello", ZonedDateTime.parse("2019-01-01T09:00:59+00:00"));
        createDocument("0.1.1", "Hi", ZonedDateTime.parse("2019-02-14T23:20:00+00:00"));
        createDocument("0.2.0", "What's up", ZonedDateTime.parse("2019-04-13T11:10:02+00:00"));

        List<AppliedMigration> appliedMigrations = migrationHistory.getAppliedMigrations();

        assertThat(appliedMigrations, hasSize(3));
        assertThat(appliedMigrations.get(0).getVersion(), equalTo(new MigrationVersion("0.1.0")));
        assertThat(appliedMigrations.get(0).getDescription(), equalTo("Hello"));
        assertThat(appliedMigrations.get(0).getExecutedAt(), equalTo(ZonedDateTime.parse("2019-01-01T09:00:59+00:00")));
        assertThat(appliedMigrations.get(1).getVersion(), equalTo(new MigrationVersion("0.1.1")));
        assertThat(appliedMigrations.get(1).getDescription(), equalTo("Hi"));
        assertThat(appliedMigrations.get(1).getExecutedAt(), equalTo(ZonedDateTime.parse("2019-02-14T23:20:00+00:00")));
        assertThat(appliedMigrations.get(2).getVersion(), equalTo(new MigrationVersion("0.2.0")));
        assertThat(appliedMigrations.get(2).getDescription(), equalTo("What's up"));
        assertThat(appliedMigrations.get(2).getExecutedAt(), equalTo(ZonedDateTime.parse("2019-04-13T11:10:02+00:00")));
    }

    @Test
    public void getAppliedMigrations_GivenMigrationDoesNotExist() {
        List<AppliedMigration> appliedMigrations = migrationHistory.getAppliedMigrations();

        assertThat(appliedMigrations, hasSize(0));
    }

    @Test
    public void addAppliedMigration() {
        AppliedMigration appliedMigration = new AppliedMigration(new MigrationVersion("1"), "Add fields", ZonedDateTime.parse("2019-01-01T09:00:59+00:00"));

        migrationHistory.addAppliedMigration(appliedMigration);

        List<Document> documents = findAll(database, SCHEMA_MIGRATIONS_COLLECTION, Sorts.descending("executedAt"));
        assertThat(documents, hasSize(1));
        assertThat(documents.get(0).getString("version"), equalTo("1.0.0"));
        assertThat(documents.get(0).getString("description"), equalTo("Add fields"));
        assertThat(documents.get(0).getDate("executedAt"), equalTo(Date.from(ZonedDateTime.parse("2019-01-01T09:00:59+00:00").toInstant())));
    }

    @Test
    public void removeAppliedMigration() {
        createDocument("0.1.0", "Hello", ZonedDateTime.parse("2019-01-01T09:00:59+00:00"));
        createDocument("0.1.1", "Hi", ZonedDateTime.parse("2019-02-14T23:20:00+00:00"));
        AppliedMigration appliedMigration = new AppliedMigration(new MigrationVersion("0.1.1"), "Hi", ZonedDateTime.parse("2019-02-14T23:20:00+00:00"));

        migrationHistory.removeAppliedMigration(appliedMigration);

        List<Document> documents = findAll(database, SCHEMA_MIGRATIONS_COLLECTION, Sorts.descending("executedAt"));
        assertThat(documents, hasSize(1));
        assertThat(documents.get(0).getString("version"), equalTo("0.1.0"));
        assertThat(documents.get(0).getString("description"), equalTo("Hello"));
    }

    @Test
    public void removeAppliedMigration_GivenEmptyMigrations() {
        AppliedMigration appliedMigration = new AppliedMigration(new MigrationVersion("0.1.1"), "Hi", ZonedDateTime.parse("2019-02-14T23:20:00+00:00"));

        migrationHistory.removeAppliedMigration(appliedMigration);

        List<Document> documents = findAll(database, SCHEMA_MIGRATIONS_COLLECTION, Sorts.descending("executedAt"));
        assertThat(documents, hasSize(0));
    }

    private void createDocument(String version, String description, ZonedDateTime executedAt) {
        Document document = new Document()
                .append("version", version)
                .append("description", description)
                .append("executedAt", Date.from(executedAt.toInstant()));
        database.getCollection(SCHEMA_MIGRATIONS_COLLECTION).insertOne(document);
    }
}
