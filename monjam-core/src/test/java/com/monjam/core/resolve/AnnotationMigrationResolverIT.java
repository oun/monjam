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

public class AnnotationMigrationResolverIT {
    private AnnotationMigrationResolver migrationResolver;

    @Before
    public void setup() {
        Configuration configuration = new Configuration();
        configuration.setLocation("db/migration/annotation");
        migrationResolver = new AnnotationMigrationResolver(configuration);
    }

    @Test
    public void resolveMigrations_GivenTypeMigrate() {
        List<ResolvedMigration> migrations = migrationResolver.resolveMigrations(MigrationType.MIGRATE);

        assertThat(migrations, hasSize(3));
        assertThat(migrations.get(0).getVersion(), equalTo(new MigrationVersion("0.1.2")));
        assertThat(migrations.get(0).getDescription(), equalTo("Add marital status"));

        assertThat(migrations.get(1).getVersion(), equalTo(new MigrationVersion("0.2.2")));
        assertThat(migrations.get(1).getDescription(), equalTo("Add username index"));

        assertThat(migrations.get(2).getVersion(), equalTo(new MigrationVersion("0.3.2")));
        assertThat(migrations.get(2).getDescription(), equalTo("Add read flag to message"));
    }

    @Test
    public void resolveMigrations_GivenTypeRollback() {
        List<ResolvedMigration> migrations = migrationResolver.resolveMigrations(MigrationType.ROLLBACK);

        assertThat(migrations, hasSize(3));
        assertThat(migrations.get(0).getVersion(), equalTo(new MigrationVersion("0.1.2")));
        assertThat(migrations.get(0).getDescription(), equalTo("Revert add marital status"));

        assertThat(migrations.get(1).getVersion(), equalTo(new MigrationVersion("0.2.2")));
        assertThat(migrations.get(1).getDescription(), equalTo("Revert add username index"));

        assertThat(migrations.get(2).getVersion(), equalTo(new MigrationVersion("0.3.2")));
        assertThat(migrations.get(2).getDescription(), equalTo("Revert add read flag to message"));
    }
}
