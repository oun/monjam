package com.monjam.core.command;

import com.mongodb.client.MongoDatabase;
import com.monjam.core.api.Configuration;
import com.monjam.core.api.Migration;
import com.monjam.core.api.MigrationVersion;
import com.monjam.core.history.AppliedMigration;
import com.monjam.core.history.MigrationHistory;
import com.monjam.core.resolve.JavaResolvedMigration;
import com.monjam.core.resolve.MigrationResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZonedDateTime.class})
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
        ZonedDateTime now = ZonedDateTime.of(2019, 6, 1, 9, 0, 0, 0, ZoneId.systemDefault());
        mockStatic(ZonedDateTime.class);
        when(ZonedDateTime.now()).thenReturn(now);
        when(migrationResolver.resolveMigrations()).thenReturn(Arrays.asList(
                new JavaResolvedMigration(new MigrationVersion("0.1"), "", mock(Migration.class)),
                new JavaResolvedMigration(new MigrationVersion("0.1.1"), "", mock(Migration.class)),
                new JavaResolvedMigration(new MigrationVersion("0.2.0"), "", mock(Migration.class))
        ));
        when(migrationHistory.getAppliedMigrations()).thenReturn(Collections.emptyList());

        command.doExecute(database, migrationResolver, migrationHistory);

        InOrder inOrder = inOrder(migrationHistory);
        inOrder.verify(migrationHistory).addAppliedMigration(eq(new AppliedMigration(new MigrationVersion("0.1"), "", now)));
        inOrder.verify(migrationHistory).addAppliedMigration(eq(new AppliedMigration(new MigrationVersion("0.1.1"), "", now)));
        inOrder.verify(migrationHistory).addAppliedMigration(eq(new AppliedMigration(new MigrationVersion("0.2.0"), "", now)));
    }
}
