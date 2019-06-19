package com.monjam.core.resolve;

import com.monjam.core.api.Configuration;

import java.util.Collections;
import java.util.List;

public class JavaMigrationResolver implements MigrationResolver {
    public JavaMigrationResolver(Configuration configuration) {

    }

    @Override
    public List<ResolvedMigration> resolveMigrations() {
        return Collections.emptyList();
    }
}
