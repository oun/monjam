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
                .location("db/migration/success")
                .build();
        migrationResolver = new JavaMigrationResolver(configuration);
    }

    @Test
    public void resolveMigrations() {
        List<ResolvedMigration> migrations = migrationResolver.resolveMigrations();

        assertThat(migrations, hasSize(3));
        assertThat(migrations.get(0).getVersion(), equalTo(new MigrationVersion("0.1.0")));
        assertThat(migrations.get(0).getDescription(), equalTo("Add prefix to user"));

        assertThat(migrations.get(1).getVersion(), equalTo(new MigrationVersion("0.2.0")));
        assertThat(migrations.get(1).getDescription(), equalTo("Remove nickname from user"));

        assertThat(migrations.get(2).getVersion(), equalTo(new MigrationVersion("0.3.0")));
        assertThat(migrations.get(2).getDescription(), equalTo("Update user gender"));
    }
}
