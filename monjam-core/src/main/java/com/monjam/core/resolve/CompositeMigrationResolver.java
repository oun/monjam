package com.monjam.core.resolve;

import com.monjam.core.api.MigrationType;
import com.monjam.core.api.MigrationVersion;
import com.monjam.core.api.MonJamException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompositeMigrationResolver implements MigrationResolver {
    private List<MigrationResolver> resolvers;

    public CompositeMigrationResolver(MigrationResolver... resolvers) {
        this.resolvers = Arrays.asList(resolvers);
    }

    @Override
    public List<ResolvedMigration> resolveMigrations(MigrationType type) {
        List<ResolvedMigration> migrations = resolvers.stream()
                .flatMap(resolver -> resolver.resolveMigrations(type).stream())
                .sorted(Comparator.comparing(ResolvedMigration::getVersion))
                .collect(Collectors.toList());
        List<MigrationVersion> versions = checkDuplicatedVersion(migrations);
        if (!versions.isEmpty()) {
            throw new MonJamException("Duplicated migration version " + versions);
        }
        return migrations;
    }

    private List<MigrationVersion> checkDuplicatedVersion(List<ResolvedMigration> resolvedMigrations) {
        return resolvedMigrations.stream()
                .map(ResolvedMigration::getVersion)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }
}
