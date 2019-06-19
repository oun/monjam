package com.monjam.core.command;

import com.mongodb.client.MongoDatabase;
import com.monjam.core.api.Configuration;
import com.monjam.core.api.Migration;
import com.monjam.core.api.MigrationVersion;
import com.monjam.core.history.MigrationHistory;
import com.monjam.core.resolve.JavaResolvedMigration;
import com.monjam.core.resolve.MigrationResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DbMigrateTest {
    private DbMigrate command;
    @Mock
    private MongoDatabase database;
    @Mock
    private MigrationHistory migrationHistory;
    @Mock
    private MigrationResolver migrationResolver;

    @Before
    public void setup() {
        command = new DbMigrate(new Configuration());
    }

    @Test
    public void doExecute_GivenEmptyAppliedMigration() {
        when(migrationResolver.resolveMigrations()).thenReturn(Arrays.asList(
                new JavaResolvedMigration(new MigrationVersion("0.1"), "", mock(Migration.class)),
                new JavaResolvedMigration(new MigrationVersion("0.1.1"), "", mock(Migration.class)),
                new JavaResolvedMigration(new MigrationVersion("0.2.0"), "", mock(Migration.class))
        ));
        when(migrationHistory.getAppliedMigrations()).thenReturn(Collections.emptyList());

        command.doExecute(database, migrationResolver, migrationHistory);
    }
}
