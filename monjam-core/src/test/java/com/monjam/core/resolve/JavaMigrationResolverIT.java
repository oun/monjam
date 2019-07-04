package com.monjam.core.resolve;

import com.monjam.core.api.Configuration;
import com.monjam.core.api.MigrationVersion;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class JavaMigrationResolverIT {
    private JavaMigrationResolver migrationResolver;

    @Before
    public void setup() {
        Configuration configuration = Configuration.builder()
                .location("db/migration,com.monjam.core.db.migration")
                .build();
        migrationResolver = new JavaMigrationResolver(configuration);
    }

    @Test
    public void resolveMigrations() {
        List<ResolvedMigration> migrations = migrationResolver.resolveMigrations();

        assertThat(migrations, hasSize(3));
        assertThat(migrations.get(0).getVersion(), equalTo(new MigrationVersion("0.1.0")));
        assertThat(migrations.get(0).getDescription(), equalTo("Create Collection"));

        assertThat(migrations.get(1).getVersion(), equalTo(new MigrationVersion("0.1.1")));
        assertThat(migrations.get(1).getDescription(), equalTo("Update Document"));

        assertThat(migrations.get(2).getVersion(), equalTo(new MigrationVersion("0.1.2")));
        assertThat(migrations.get(2).getDescription(), equalTo("Create Index"));
    }
}
