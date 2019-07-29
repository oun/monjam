package com.monjam.core.resolve;

import com.monjam.core.api.MigrationType;
import com.monjam.core.api.MigrationVersion;
import com.monjam.core.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class ScriptMigrationResolverIT {
    private ScriptMigrationResolver migrationResolver;

    @Before
    public void setup() {
        Configuration configuration = new Configuration();
        configuration.setLocation("db/migration");
        migrationResolver = new ScriptMigrationResolver(configuration);
    }

    @Test
    public void resolveMigrations_GivenTypeMigrate() {
        List<ResolvedMigration> migrations = migrationResolver.resolveMigrations(MigrationType.MIGRATE);

        assertThat(migrations, hasSize(3));
        assertThat(migrations.get(0).getVersion(), equalTo(new MigrationVersion("0.1.1")));
        assertThat(migrations.get(0).getDescription(), equalTo("Add status to user"));

        assertThat(migrations.get(1).getVersion(), equalTo(new MigrationVersion("0.2.1")));
        assertThat(migrations.get(1).getDescription(), equalTo("Remove age from user"));

        assertThat(migrations.get(2).getVersion(), equalTo(new MigrationVersion("0.3.1")));
        assertThat(migrations.get(2).getDescription(), equalTo("Update user last name"));
    }

    @Test
    public void resolveMigrations_GivenTypeRollback() {
        List<ResolvedMigration> migrations = migrationResolver.resolveMigrations(MigrationType.ROLLBACK);

        assertThat(migrations, hasSize(2));
        assertThat(migrations.get(0).getVersion(), equalTo(new MigrationVersion("0.1.1")));
        assertThat(migrations.get(0).getDescription(), equalTo("Revert add status to user"));

        assertThat(migrations.get(1).getVersion(), equalTo(new MigrationVersion("0.3.1")));
        assertThat(migrations.get(1).getDescription(), equalTo("Revert update user last name"));
    }
}
