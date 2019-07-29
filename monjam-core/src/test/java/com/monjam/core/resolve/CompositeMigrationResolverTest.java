package com.monjam.core.resolve;

import com.monjam.core.api.MigrationType;
import com.monjam.core.api.MigrationVersion;
import com.monjam.core.api.MonJamException;
import com.monjam.core.executor.MigrationExecutor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompositeMigrationResolverTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private MigrationResolver resolver1;
    private MigrationResolver resolver2;

    @Before
    public void setup() {
        resolver1 = mock(MigrationResolver.class);
        resolver2 = mock(MigrationResolver.class);
    }

    @Test
    public void resolveMigration_GivenDistinctMigrationVersions() {
        when(resolver1.resolveMigrations(MigrationType.MIGRATE)).thenReturn(Arrays.asList(
                mockResolvedMigration("0.1.0", "1st Resolver1"),
                mockResolvedMigration("0.3.0", "3rd Resolver1"))
        );
        when(resolver2.resolveMigrations(MigrationType.MIGRATE)).thenReturn(Arrays.asList(
                mockResolvedMigration("0.2.0", "2nd Resolver2"),
                mockResolvedMigration("0.3.1", "4th Resolver2"))
        );
        MigrationResolver compositeResolver = new CompositeMigrationResolver(resolver1, resolver2);

        List<ResolvedMigration> migrations = compositeResolver.resolveMigrations(MigrationType.MIGRATE);

        assertThat(migrations, hasSize(4));
        assertThat(migrations.get(0).getVersion().toString(), equalTo("0.1.0"));
        assertThat(migrations.get(0).getDescription(), equalTo("1st Resolver1"));
        assertThat(migrations.get(1).getVersion().toString(), equalTo("0.2.0"));
        assertThat(migrations.get(1).getDescription(), equalTo("2nd Resolver2"));
        assertThat(migrations.get(2).getVersion().toString(), equalTo("0.3.0"));
        assertThat(migrations.get(2).getDescription(), equalTo("3rd Resolver1"));
        assertThat(migrations.get(3).getVersion().toString(), equalTo("0.3.1"));
        assertThat(migrations.get(3).getDescription(), equalTo("4th Resolver2"));
    }

    @Test
    public void resolveMigration_GivenDuplicatedMigrationVersions() {
        when(resolver1.resolveMigrations(MigrationType.MIGRATE)).thenReturn(Arrays.asList(
                mockResolvedMigration("0.1.0", "1st Resolver1"),
                mockResolvedMigration("0.3.0", "3rd Resolver1")));
        when(resolver2.resolveMigrations(MigrationType.MIGRATE)).thenReturn(Arrays.asList(
                mockResolvedMigration("0.1.0", "2nd Resolver2"),
                mockResolvedMigration("0.2.0", "2nd Resolver2"),
                mockResolvedMigration("0.3.0", "4th Resolver2"))
        );
        MigrationResolver compositeResolver = new CompositeMigrationResolver(resolver1, resolver2);

        exception.expectMessage("Duplicated migration version [0.1.0, 0.3.0]");
        exception.expect(MonJamException.class);

        compositeResolver.resolveMigrations(MigrationType.MIGRATE);
    }

    @Test
    public void resolveMigration_GivenOneResolverReturnEmptyMigrations() {
        when(resolver1.resolveMigrations(MigrationType.MIGRATE)).thenReturn(Collections.emptyList());
        when(resolver2.resolveMigrations(MigrationType.MIGRATE)).thenReturn(Arrays.asList(
                mockResolvedMigration("0.1.0", "1st Resolver2"),
                mockResolvedMigration("0.2.0", "2nd Resolver2"),
                mockResolvedMigration("0.3.0", "3rd Resolver2"))
        );
        MigrationResolver compositeResolver = new CompositeMigrationResolver(resolver1, resolver2);

        List<ResolvedMigration> migrations = compositeResolver.resolveMigrations(MigrationType.MIGRATE);

        assertThat(migrations, hasSize(3));
        assertThat(migrations.get(0).getVersion().toString(), equalTo("0.1.0"));
        assertThat(migrations.get(0).getDescription(), equalTo("1st Resolver2"));
        assertThat(migrations.get(1).getVersion().toString(), equalTo("0.2.0"));
        assertThat(migrations.get(1).getDescription(), equalTo("2nd Resolver2"));
        assertThat(migrations.get(2).getVersion().toString(), equalTo("0.3.0"));
        assertThat(migrations.get(2).getDescription(), equalTo("3rd Resolver2"));
    }

    private ResolvedMigration mockResolvedMigration(String version, String description) {
        return new ResolvedMigration() {
            @Override
            public MigrationVersion getVersion() {
                return new MigrationVersion(version);
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public MigrationExecutor getExecutor() {
                return null;
            }
        };
    }
}
