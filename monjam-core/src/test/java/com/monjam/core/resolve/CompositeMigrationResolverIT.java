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

public class CompositeMigrationResolverIT {
    private CompositeMigrationResolver migrationResolver;

    @Before
    public void setup() {
        Configuration configuration = new Configuration();
        configuration.setLocation("db/migration/success,db/migration/script/success");
        MigrationResolver javaMigrationResolver = new JavaMigrationResolver(configuration);
        MigrationResolver scriptMigrationResolver = new ScriptMigrationResolver(configuration);
        migrationResolver = new CompositeMigrationResolver(javaMigrationResolver, scriptMigrationResolver);
    }

    @Test
    public void resolveMigrations_GivenTypeMigrate() {
        List<ResolvedMigration> migrations = migrationResolver.resolveMigrations(MigrationType.MIGRATE);

        assertThat(migrations, hasSize(6));
        assertThat(migrations.get(0).getVersion(), equalTo(new MigrationVersion("0.1.0")));
        assertThat(migrations.get(0).getDescription(), equalTo("Add prefix to user"));

        assertThat(migrations.get(1).getVersion(), equalTo(new MigrationVersion("0.1.1")));
        assertThat(migrations.get(1).getDescription(), equalTo("Add status to user"));

        assertThat(migrations.get(2).getVersion(), equalTo(new MigrationVersion("0.2.0")));
        assertThat(migrations.get(2).getDescription(), equalTo("Remove nickname from user"));

        assertThat(migrations.get(3).getVersion(), equalTo(new MigrationVersion("0.2.1")));
        assertThat(migrations.get(3).getDescription(), equalTo("Remove age from user"));

        assertThat(migrations.get(4).getVersion(), equalTo(new MigrationVersion("0.3.0")));
        assertThat(migrations.get(4).getDescription(), equalTo("Update user gender"));

        assertThat(migrations.get(5).getVersion(), equalTo(new MigrationVersion("0.3.1")));
        assertThat(migrations.get(5).getDescription(), equalTo("Update user last name"));
    }

    @Test
    public void resolveMigrations_GivenTypeRollback() {
        List<ResolvedMigration> migrations = migrationResolver.resolveMigrations(MigrationType.ROLLBACK);

        assertThat(migrations, hasSize(5));
        assertThat(migrations.get(0).getVersion(), equalTo(new MigrationVersion("0.1.0")));
        assertThat(migrations.get(0).getDescription(), equalTo("Add prefix to user"));

        assertThat(migrations.get(1).getVersion(), equalTo(new MigrationVersion("0.1.1")));
        assertThat(migrations.get(1).getDescription(), equalTo("Revert add status to user"));

        assertThat(migrations.get(2).getVersion(), equalTo(new MigrationVersion("0.2.0")));
        assertThat(migrations.get(2).getDescription(), equalTo("Remove nickname from user"));

        assertThat(migrations.get(3).getVersion(), equalTo(new MigrationVersion("0.3.0")));
        assertThat(migrations.get(3).getDescription(), equalTo("Update user gender"));

        assertThat(migrations.get(4).getVersion(), equalTo(new MigrationVersion("0.3.1")));
        assertThat(migrations.get(4).getDescription(), equalTo("Revert update user last name"));
    }
}
