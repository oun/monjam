package com.monjam.core.command;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.monjam.core.configuration.Configuration;
import com.monjam.core.api.Context;
import com.monjam.core.api.MigrationVersion;
import com.monjam.core.api.MonJamException;
import com.monjam.core.executor.JavaMigrationExecutor;
import com.monjam.core.history.AppliedMigration;
import com.monjam.core.history.MigrationHistory;
import com.monjam.core.resolve.JavaResolvedMigration;
import com.monjam.core.resolve.MigrationResolver;
import com.monjam.core.resolve.ResolvedMigration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
public class DbRollbackTest {
    private DbRollback command;

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

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        command = new DbRollback(new Configuration());
        mockStatic(ZonedDateTime.class);
        context = new Context(client, database, session, new Configuration(), true);
    }

    @Test
    public void doExecute_GivenAppliedMigrationsExist() {
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
        when(migrationResolver.resolveMigrations()).thenReturn(resolvedMigrations);
        when(migrationHistory.getAppliedMigrations()).thenReturn(appliedMigrations);

        command.doExecute(context, migrationResolver, migrationHistory);

        verify(migration_0_1.getExecutor(), never()).executeDown(any(Context.class));
        verify(migration_0_1_1.getExecutor(), times(1)).executeDown(any(Context.class));
        verify(migration_0_2.getExecutor(), never()).executeDown(any(Context.class));

        verify(migrationHistory, never()).removeAppliedMigration(eq(new AppliedMigration(migration_0_2.getVersion(), "", executedAt)));
        verify(migrationHistory, never()).removeAppliedMigration(eq(new AppliedMigration(migration_0_1.getVersion(), "", executedAt)));
        verify(migrationHistory, times(1)).removeAppliedMigration(eq(new AppliedMigration(migration_0_1_1.getVersion(), "", executedAt)));
    }

    @Test
    public void doExecute_GivenAppliedMigrationsDoesNotExist() {
        ZonedDateTime executedAt = ZonedDateTime.of(2019, 6, 20, 9, 0, 0, 0, ZoneId.systemDefault());
        ResolvedMigration migration_0_1 = new JavaResolvedMigration(new MigrationVersion("0.1"), "", mock(JavaMigrationExecutor.class));
        ResolvedMigration migration_0_1_1 = new JavaResolvedMigration(new MigrationVersion("0.1.1"), "", mock(JavaMigrationExecutor.class));
        ResolvedMigration migration_0_2 = new JavaResolvedMigration(new MigrationVersion("0.2.0"), "", mock(JavaMigrationExecutor.class));
        List<ResolvedMigration> resolvedMigrations = Arrays.asList(migration_0_1, migration_0_1_1, migration_0_2);
        List<AppliedMigration> appliedMigrations = Collections.emptyList();
        when(ZonedDateTime.now()).thenReturn(executedAt);
        when(migrationResolver.resolveMigrations()).thenReturn(resolvedMigrations);
        when(migrationHistory.getAppliedMigrations()).thenReturn(appliedMigrations);

        command.doExecute(context, migrationResolver, migrationHistory);

        verify(migration_0_1.getExecutor(), never()).executeDown(any(Context.class));
        verify(migration_0_1_1.getExecutor(), never()).executeDown(any(Context.class));
        verify(migration_0_2.getExecutor(), never()).executeDown(any(Context.class));

        verify(migrationHistory, never()).removeAppliedMigration(eq(new AppliedMigration(migration_0_2.getVersion(), "", executedAt)));
        verify(migrationHistory, never()).removeAppliedMigration(eq(new AppliedMigration(migration_0_1.getVersion(), "", executedAt)));
        verify(migrationHistory, never()).removeAppliedMigration(eq(new AppliedMigration(migration_0_1_1.getVersion(), "", executedAt)));
    }

    @Test
    public void doExecute_GivenResolvedMigrationsDoesNotExist() {
        ZonedDateTime executedAt = ZonedDateTime.of(2019, 6, 20, 9, 0, 0, 0, ZoneId.systemDefault());
        ResolvedMigration migration_0_1 = new JavaResolvedMigration(new MigrationVersion("0.1"), "", mock(JavaMigrationExecutor.class));
        ResolvedMigration migration_0_1_1 = new JavaResolvedMigration(new MigrationVersion("0.1.1"), "", mock(JavaMigrationExecutor.class));
        List<ResolvedMigration> resolvedMigrations = Arrays.asList(migration_0_1, migration_0_1_1);
        List<AppliedMigration> appliedMigrations = Arrays.asList(
                new AppliedMigration(new MigrationVersion("0.1.0"), "", executedAt),
                new AppliedMigration(new MigrationVersion("0.1.1"), "", executedAt),
                new AppliedMigration(new MigrationVersion("0.1.2"), "", executedAt)
        );
        when(ZonedDateTime.now()).thenReturn(executedAt);
        when(migrationResolver.resolveMigrations()).thenReturn(resolvedMigrations);
        when(migrationHistory.getAppliedMigrations()).thenReturn(appliedMigrations);

        exception.expect(MonJamException.class);
        exception.expectMessage("Could not execute rollback migration version 0.1.2. Migration not found");

        command.doExecute(context, migrationResolver, migrationHistory);

        verify(migration_0_1.getExecutor(), never()).executeDown(any(Context.class));
        verify(migration_0_1_1.getExecutor(), times(1)).executeDown(any(Context.class));

        verify(migrationHistory, never()).removeAppliedMigration(eq(new AppliedMigration(migration_0_1.getVersion(), "", executedAt)));
        verify(migrationHistory, never()).removeAppliedMigration(eq(new AppliedMigration(migration_0_1_1.getVersion(), "", executedAt)));
    }
}
