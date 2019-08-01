package com.monjam.core.command;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.monjam.core.api.Context;
import com.monjam.core.api.MigrationType;
import com.monjam.core.api.MigrationVersion;
import com.monjam.core.api.MonJamException;
import com.monjam.core.configuration.Configuration;
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
    @Rule
    public ExpectedException exception = ExpectedException.none();

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
    private ZonedDateTime executedAt;

    @Before
    public void setup() {
        command = new DbMigrate(new Configuration());
        context = new Context(client, database, session, new Configuration(), true);
        executedAt = ZonedDateTime.of(2019, 6, 20, 9, 0, 0, 0, ZoneId.systemDefault());
        mockStatic(ZonedDateTime.class);
        when(ZonedDateTime.now()).thenReturn(executedAt);
    }

    @Test
    public void doExecute_GivenEmptyAppliedMigrations() {
        ResolvedMigration resolved_0_1 = resolvedMigration("0.1");
        ResolvedMigration resolved_0_1_1 = resolvedMigration("0.1.1");
        ResolvedMigration resolved_0_2 = resolvedMigration("0.2.0");
        List<ResolvedMigration> resolvedMigrations = Arrays.asList(resolved_0_1, resolved_0_1_1, resolved_0_2);
        when(migrationResolver.resolveMigrations(eq(MigrationType.MIGRATE))).thenReturn(resolvedMigrations);
        List<AppliedMigration> appliedMigrations = Collections.emptyList();
        when(migrationHistory.getAppliedMigrations()).thenReturn(appliedMigrations);

        command.doExecute(context, migrationResolver, migrationHistory);

        verify(resolved_0_1.getExecutor(), only()).execute(any(Context.class));
        verify(resolved_0_1_1.getExecutor(), only()).execute(any(Context.class));
        verify(resolved_0_2.getExecutor(), only()).execute(any(Context.class));

        InOrder inOrder = inOrder(migrationHistory);
        inOrder.verify(migrationHistory).addAppliedMigration(eq(appliedMigration("0.1")));
        inOrder.verify(migrationHistory).addAppliedMigration(eq(appliedMigration("0.1.1")));
        inOrder.verify(migrationHistory).addAppliedMigration(eq(appliedMigration("0.2")));
    }

    @Test
    public void doExecute_GivenEmptyResolvedMigrations() {
        List<ResolvedMigration> resolvedMigrations = Collections.emptyList();
        AppliedMigration applied_0_1_0 = appliedMigration("0.1.0");
        AppliedMigration applied_0_1_1 = appliedMigration("0.1.1");
        AppliedMigration applied_0_2_0 = appliedMigration("0.2.0");
        List<AppliedMigration> appliedMigrations = Arrays.asList(applied_0_1_0, applied_0_1_1, applied_0_2_0);
        when(migrationHistory.getAppliedMigrations()).thenReturn(appliedMigrations);
        when(migrationResolver.resolveMigrations(eq(MigrationType.MIGRATE))).thenReturn(resolvedMigrations);

        command.doExecute(context, migrationResolver, migrationHistory);

        verify(migrationHistory, never()).addAppliedMigration(any(AppliedMigration.class));
    }

    @Test
    public void doExecute_GivenLastAppliedMigrationVersionLessThanLastResolvedMigrationVersion() {
        ResolvedMigration migration_0_1 = resolvedMigration("0.1");
        ResolvedMigration migration_0_1_1 = resolvedMigration("0.1.1");
        ResolvedMigration migration_0_2 = resolvedMigration("0.2.0");
        List<ResolvedMigration> resolvedMigrations = Arrays.asList(migration_0_1, migration_0_1_1, migration_0_2);
        when(migrationResolver.resolveMigrations(eq(MigrationType.MIGRATE))).thenReturn(resolvedMigrations);
        AppliedMigration applied_0_1_0 = appliedMigration("0.1.0");
        AppliedMigration applied_0_1_1 = appliedMigration("0.1.1");
        List<AppliedMigration> appliedMigrations = Arrays.asList(applied_0_1_0, applied_0_1_1);
        when(migrationHistory.getAppliedMigrations()).thenReturn(appliedMigrations);

        command.doExecute(context, migrationResolver, migrationHistory);

        verify(migration_0_1.getExecutor(), never()).execute(any(Context.class));
        verify(migration_0_1_1.getExecutor(), never()).execute(any(Context.class));
        verify(migration_0_2.getExecutor(), times(1)).execute(any(Context.class));

        verify(migrationHistory, times(1)).addAppliedMigration(eq(appliedMigration("0.2.0")));
        verify(migrationHistory, never()).addAppliedMigration(eq(appliedMigration("0.1.0")));
        verify(migrationHistory, never()).addAppliedMigration(eq(appliedMigration("0.1.1")));
    }

    @Test
    public void doExecute_GivenLastAppliedMigrationVersionGreaterThanLastResolvedMigrationVersion() {
        ResolvedMigration resolved_0_1 = resolvedMigration("0.1");
        ResolvedMigration resolved_0_1_1 = resolvedMigration("0.1.1");
        ResolvedMigration resolved_0_2 = resolvedMigration("0.2");
        List<ResolvedMigration> resolvedMigrations = Arrays.asList(resolved_0_1, resolved_0_1_1, resolved_0_2);
        when(migrationResolver.resolveMigrations(eq(MigrationType.MIGRATE))).thenReturn(resolvedMigrations);
        AppliedMigration applied_0_1_0 = appliedMigration("0.1.0");
        AppliedMigration applied_0_1_1 = appliedMigration("0.1.1");
        AppliedMigration applied_0_2_0 = appliedMigration("0.2.0");
        AppliedMigration applied_0_2_1 = appliedMigration("0.2.1");
        List<AppliedMigration> appliedMigrations = Arrays.asList(applied_0_1_0, applied_0_1_1, applied_0_2_0, applied_0_2_1);
        when(migrationHistory.getAppliedMigrations()).thenReturn(appliedMigrations);

        command.doExecute(context, migrationResolver, migrationHistory);

        verify(resolved_0_1.getExecutor(), never()).execute(any(Context.class));
        verify(resolved_0_1_1.getExecutor(), never()).execute(any(Context.class));
        verify(resolved_0_2.getExecutor(), never()).execute(any(Context.class));

        verify(migrationHistory, never()).addAppliedMigration(eq(appliedMigration("0.1")));
        verify(migrationHistory, never()).addAppliedMigration(eq(appliedMigration("0.1.1")));
        verify(migrationHistory, never()).addAppliedMigration(eq(appliedMigration("0.2")));
    }

    @Test
    public void doExecute_GivenTargetVersionGreaterThanLastAppliedMigration() {
        Configuration configuration = new Configuration();
        configuration.setTarget("0.3.0");
        command = new DbMigrate(configuration);

        ResolvedMigration resolved_0_1 = resolvedMigration("0.1");
        ResolvedMigration resolved_0_2 = resolvedMigration("0.2");
        ResolvedMigration resolved_0_3 = resolvedMigration("0.3");
        ResolvedMigration resolved_0_4 = resolvedMigration("0.4");
        List<ResolvedMigration> resolvedMigrations = Arrays.asList(resolved_0_1, resolved_0_2, resolved_0_3, resolved_0_4);
        when(migrationResolver.resolveMigrations(eq(MigrationType.MIGRATE))).thenReturn(resolvedMigrations);
        AppliedMigration applied_0_1 = appliedMigration("0.1");
        List<AppliedMigration> appliedMigrations = Arrays.asList(applied_0_1);
        when(migrationHistory.getAppliedMigrations()).thenReturn(appliedMigrations);

        command.doExecute(context, migrationResolver, migrationHistory);

        verify(resolved_0_1.getExecutor(), never()).execute(any(Context.class));
        verify(resolved_0_2.getExecutor(), times(1)).execute(any(Context.class));
        verify(resolved_0_3.getExecutor(), times(1)).execute(any(Context.class));
        verify(resolved_0_4.getExecutor(), never()).execute(any(Context.class));

        verify(migrationHistory, never()).addAppliedMigration(eq(appliedMigration("0.1")));
        verify(migrationHistory, times(1)).addAppliedMigration(eq(appliedMigration("0.2")));
        verify(migrationHistory, times(1)).addAppliedMigration(eq(appliedMigration("0.3")));
        verify(migrationHistory, never()).addAppliedMigration(eq(appliedMigration("0.4")));
    }

    @Test
    public void doExecute_GivenTargetVersionEqualsLastAppliedMigration() {
        Configuration configuration = new Configuration();
        configuration.setTarget("0.3.0");
        command = new DbMigrate(configuration);

        ResolvedMigration resolved_0_1 = resolvedMigration("0.1");
        ResolvedMigration resolved_0_2 = resolvedMigration("0.2");
        ResolvedMigration resolved_0_3 = resolvedMigration("0.3");
        ResolvedMigration resolved_0_4 = resolvedMigration("0.4");
        List<ResolvedMigration> resolvedMigrations = Arrays.asList(resolved_0_1, resolved_0_2, resolved_0_3, resolved_0_4);
        when(migrationResolver.resolveMigrations(eq(MigrationType.MIGRATE))).thenReturn(resolvedMigrations);
        AppliedMigration applied_0_1 = appliedMigration("0.1");
        AppliedMigration applied_0_2 = appliedMigration("0.2");
        AppliedMigration applied_0_3 = appliedMigration("0.3");
        List<AppliedMigration> appliedMigrations = Arrays.asList(applied_0_1, applied_0_2, applied_0_3);
        when(migrationHistory.getAppliedMigrations()).thenReturn(appliedMigrations);

        command.doExecute(context, migrationResolver, migrationHistory);

        verify(resolved_0_1.getExecutor(), never()).execute(any(Context.class));
        verify(resolved_0_2.getExecutor(), never()).execute(any(Context.class));
        verify(resolved_0_3.getExecutor(), never()).execute(any(Context.class));
        verify(resolved_0_4.getExecutor(), never()).execute(any(Context.class));

        verify(migrationHistory, never()).addAppliedMigration(eq(appliedMigration("0.1")));
        verify(migrationHistory, never()).addAppliedMigration(eq(appliedMigration("0.2")));
        verify(migrationHistory, never()).addAppliedMigration(eq(appliedMigration("0.3")));
        verify(migrationHistory, never()).addAppliedMigration(eq(appliedMigration("0.4")));
    }

    @Test
    public void doExecute_GivenTargetVersionLessThanLastAppliedMigration() {
        Configuration configuration = new Configuration();
        configuration.setTarget("0.2.0");
        command = new DbMigrate(configuration);

        ResolvedMigration resolved_0_1 = resolvedMigration("0.1");
        ResolvedMigration resolved_0_2 = resolvedMigration("0.2");
        ResolvedMigration resolved_0_3 = resolvedMigration("0.3");
        ResolvedMigration resolved_0_4 = resolvedMigration("0.4");
        List<ResolvedMigration> resolvedMigrations = Arrays.asList(resolved_0_1, resolved_0_2, resolved_0_3, resolved_0_4);
        when(migrationResolver.resolveMigrations(eq(MigrationType.MIGRATE))).thenReturn(resolvedMigrations);
        AppliedMigration applied_0_1 = appliedMigration("0.1");
        AppliedMigration applied_0_2 = appliedMigration("0.2");
        AppliedMigration applied_0_3 = appliedMigration("0.3");
        List<AppliedMigration> appliedMigrations = Arrays.asList(applied_0_1, applied_0_2, applied_0_3);
        when(migrationHistory.getAppliedMigrations()).thenReturn(appliedMigrations);

        exception.expect(MonJamException.class);

        command.doExecute(context, migrationResolver, migrationHistory);
    }

    @Test
    public void doExecute_GivenTargetVersionDoesNotExist() {
        Configuration configuration = new Configuration();
        configuration.setTarget("0.5.0");
        command = new DbMigrate(configuration);

        ResolvedMigration resolved_0_1 = resolvedMigration("0.1.0");
        ResolvedMigration resolved_0_2 = resolvedMigration("0.2.0");
        ResolvedMigration resolved_0_3 = resolvedMigration("0.3.0");
        List<ResolvedMigration> resolvedMigrations = Arrays.asList(resolved_0_1, resolved_0_2, resolved_0_3);
        when(migrationResolver.resolveMigrations(eq(MigrationType.ROLLBACK))).thenReturn(resolvedMigrations);
        AppliedMigration applied_0_1_0 = appliedMigration("0.1.0");
        AppliedMigration applied_0_2_0 = appliedMigration("0.2.0");
        List<AppliedMigration> appliedMigrations = Arrays.asList(applied_0_1_0, applied_0_2_0);
        when(migrationHistory.getAppliedMigrations()).thenReturn(appliedMigrations);

        exception.expectMessage("Target version 0.5.0 not found");
        exception.expect(MonJamException.class);

        command.doExecute(context, migrationResolver, migrationHistory);
    }

    private ResolvedMigration resolvedMigration(String version) {
        return new JavaResolvedMigration(new MigrationVersion(version), "", mock(JavaMigrationExecutor.class));
    }

    private AppliedMigration appliedMigration(String version) {
        return new AppliedMigration(new MigrationVersion(version), "", executedAt);
    }
}
