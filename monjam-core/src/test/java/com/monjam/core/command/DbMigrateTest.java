package com.monjam.core.command;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.monjam.core.api.Context;
import com.monjam.core.api.MigrationType;
import com.monjam.core.api.MigrationVersion;
import com.monjam.core.configuration.Configuration;
import com.monjam.core.executor.JavaMigrationExecutor;
import com.monjam.core.history.AppliedMigration;
import com.monjam.core.history.MigrationHistory;
import com.monjam.core.resolve.JavaResolvedMigration;
import com.monjam.core.resolve.MigrationResolver;
import com.monjam.core.resolve.ResolvedMigration;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZonedDateTime.class})
public class DbMigrateTest {
    private DbMigrate command;
    @Mock
    private MongoClient client;
    @Mock
    private MongoDatabase database;
    @Mock
    private ClientSession session;
    @Mock
    private MigrationHistory migrationHistory;
    @Mock
    private MigrationResolver migrationResolver;

    private Context context;

    @Before
    public void setup() {
        command = new DbMigrate(new Configuration());
        mockStatic(ZonedDateTime.class);
        context = new Context(client, database, session, new Configuration(), true);
    }

    @Test
    public void doExecute_GivenEmptyAppliedMigrations() {
        ZonedDateTime executedAt = ZonedDateTime.of(2019, 6, 20, 9, 0, 0, 0, ZoneId.systemDefault());
        ResolvedMigration migration_0_1 = new JavaResolvedMigration(new MigrationVersion("0.1"), "", mock(JavaMigrationExecutor.class));
        ResolvedMigration migration_0_1_1 = new JavaResolvedMigration(new MigrationVersion("0.1.1"), "", mock(JavaMigrationExecutor.class));
        ResolvedMigration migration_0_2 = new JavaResolvedMigration(new MigrationVersion("0.2.0"), "", mock(JavaMigrationExecutor.class));
        List<ResolvedMigration> resolvedMigrations = Arrays.asList(migration_0_1, migration_0_1_1, migration_0_2);
        List<AppliedMigration> appliedMigrations = Collections.emptyList();
        when(ZonedDateTime.now()).thenReturn(executedAt);
        when(migrationResolver.resolveMigrations(eq(MigrationType.MIGRATE))).thenReturn(resolvedMigrations);
        when(migrationHistory.getAppliedMigrations()).thenReturn(appliedMigrations);

        command.doExecute(context, migrationResolver, migrationHistory);

        verify(migration_0_1.getExecutor(), only()).execute(any(Context.class));
        verify(migration_0_1_1.getExecutor(), only()).execute(any(Context.class));
        verify(migration_0_2.getExecutor(), only()).execute(any(Context.class));

        InOrder inOrder = inOrder(migrationHistory);
        inOrder.verify(migrationHistory).addAppliedMigration(eq(new AppliedMigration(migration_0_1.getVersion(), "", executedAt)));
        inOrder.verify(migrationHistory).addAppliedMigration(eq(new AppliedMigration(migration_0_1_1.getVersion(), "", executedAt)));
        inOrder.verify(migrationHistory).addAppliedMigration(eq(new AppliedMigration(migration_0_2.getVersion(), "", executedAt)));
    }

    @Test
    public void doExecute_GivenEmptyResolvedMigrations() {
        ZonedDateTime executedAt = ZonedDateTime.of(2019, 6, 20, 9, 0, 0, 0, ZoneId.systemDefault());
        List<ResolvedMigration> resolvedMigrations = Collections.emptyList();
        List<AppliedMigration> appliedMigrations = Arrays.asList(
                new AppliedMigration(new MigrationVersion("0.1.0"), "", executedAt),
                new AppliedMigration(new MigrationVersion("0.1.1"), "", executedAt),
                new AppliedMigration(new MigrationVersion("0.2.0"), "", executedAt)
        );
        when(ZonedDateTime.now()).thenReturn(executedAt);
        when(migrationResolver.resolveMigrations(eq(MigrationType.MIGRATE))).thenReturn(resolvedMigrations);
        when(migrationHistory.getAppliedMigrations()).thenReturn(appliedMigrations);

        command.doExecute(context, migrationResolver, migrationHistory);

        verify(migrationHistory, never()).addAppliedMigration(any(AppliedMigration.class));
    }

    @Test
    public void doExecute_GivenLastAppliedMigrationVersionLessThanLastResolvedMigrationVersion() {
        ZonedDateTime executedAt = ZonedDateTime.of(2019, 6, 20, 9, 0, 0, 0, ZoneId.systemDefault());
        ResolvedMigration migration_0_1 = new JavaResolvedMigration(new MigrationVersion("0.1"), "", mock(JavaMigrationExecutor.class));
        ResolvedMigration migration_0_1_1 = new JavaResolvedMigration(new MigrationVersion("0.1.1"), "", mock(JavaMigrationExecutor.class));
        ResolvedMigration migration_0_2 = new JavaResolvedMigration(new MigrationVersion("0.2.0"), "", mock(JavaMigrationExecutor.class));
        List<ResolvedMigration> resolvedMigrations = Arrays.asList(migration_0_1, migration_0_1_1, migration_0_2);
        List<AppliedMigration> appliedMigrations = Arrays.asList(
                new AppliedMigration(new MigrationVersion("0.1.0"), "", executedAt),
                new AppliedMigration(new MigrationVersion("0.1.1"), "", executedAt)
        );
        when(ZonedDateTime.now()).thenReturn(executedAt);
        when(migrationResolver.resolveMigrations(eq(MigrationType.MIGRATE))).thenReturn(resolvedMigrations);
        when(migrationHistory.getAppliedMigrations()).thenReturn(appliedMigrations);

        command.doExecute(context, migrationResolver, migrationHistory);

        verify(migration_0_1.getExecutor(), never()).execute(any(Context.class));
        verify(migration_0_1_1.getExecutor(), never()).execute(any(Context.class));
        verify(migration_0_2.getExecutor(), times(1)).execute(any(Context.class));

        verify(migrationHistory, times(1)).addAppliedMigration(eq(new AppliedMigration(migration_0_2.getVersion(), "", executedAt)));
        verify(migrationHistory, never()).addAppliedMigration(eq(new AppliedMigration(migration_0_1.getVersion(), "", executedAt)));
        verify(migrationHistory, never()).addAppliedMigration(eq(new AppliedMigration(migration_0_1_1.getVersion(), "", executedAt)));
    }

    @Test
    public void doExecute_GivenLastAppliedMigrationVersionGreaterThanLastResolvedMigrationVersion() {
        ZonedDateTime executedAt = ZonedDateTime.of(2019, 6, 20, 9, 0, 0, 0, ZoneId.systemDefault());
        ResolvedMigration migration_0_1 = new JavaResolvedMigration(new MigrationVersion("0.1"), "", mock(JavaMigrationExecutor.class));
        ResolvedMigration migration_0_1_1 = new JavaResolvedMigration(new MigrationVersion("0.1.1"), "", mock(JavaMigrationExecutor.class));
        ResolvedMigration migration_0_2 = new JavaResolvedMigration(new MigrationVersion("0.2.0"), "", mock(JavaMigrationExecutor.class));
        List<ResolvedMigration> resolvedMigrations = Arrays.asList(migration_0_1, migration_0_1_1, migration_0_2);
        List<AppliedMigration> appliedMigrations = Arrays.asList(
                new AppliedMigration(new MigrationVersion("0.1.0"), "", executedAt),
                new AppliedMigration(new MigrationVersion("0.1.1"), "", executedAt),
                new AppliedMigration(new MigrationVersion("0.2.0"), "", executedAt),
                new AppliedMigration(new MigrationVersion("0.2.1"), "", executedAt)
        );
        when(ZonedDateTime.now()).thenReturn(executedAt);
        when(migrationResolver.resolveMigrations(eq(MigrationType.MIGRATE))).thenReturn(resolvedMigrations);
        when(migrationHistory.getAppliedMigrations()).thenReturn(appliedMigrations);

        command.doExecute(context, migrationResolver, migrationHistory);

        verify(migration_0_1.getExecutor(), never()).execute(any(Context.class));
        verify(migration_0_1_1.getExecutor(), never()).execute(any(Context.class));
        verify(migration_0_2.getExecutor(), never()).execute(any(Context.class));

        verify(migrationHistory, never()).addAppliedMigration(eq(new AppliedMigration(migration_0_1.getVersion(), "", executedAt)));
        verify(migrationHistory, never()).addAppliedMigration(eq(new AppliedMigration(migration_0_1_1.getVersion(), "", executedAt)));
        verify(migrationHistory, never()).addAppliedMigration(eq(new AppliedMigration(migration_0_2.getVersion(), "", executedAt)));
    }
}
