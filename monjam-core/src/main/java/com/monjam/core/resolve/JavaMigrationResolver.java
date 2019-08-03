package com.monjam.core.resolve;

import com.monjam.core.api.MigrationType;
import com.monjam.core.configuration.Configuration;
import com.monjam.core.api.Migration;
import com.monjam.core.api.MonJamException;
import com.monjam.core.support.ClassUtils;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JavaMigrationResolver implements MigrationResolver {
    private static final Logger LOG = LoggerFactory.getLogger(JavaMigrationResolver.class);
    private Configuration configuration;

    public JavaMigrationResolver(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public List<ResolvedMigration> resolveMigrations(MigrationType type) {
        if (configuration.getLocation() == null) {
            throw new MonJamException("Missing migration location");
        }
        LOG.info("Scanning migration classes in {}", configuration.getLocation());
        String[] locations = Arrays.stream(configuration.getLocation().split(","))
                .map(String::trim)
                .toArray(String[]::new);
        List<ResolvedMigration> resolvedMigrations = new ArrayList<>();
        try(ScanResult scanResult = new ClassGraph()
                .addClassLoader(configuration.getClassLoader())
                .enableAllInfo()
                .whitelistPackages(locations)
                .scan()
        ) {
            for (Class<?> migrationClass : scanResult.getClassesImplementing(Migration.class.getName()).loadClasses()) {
                String migrationName = migrationClass.getSimpleName();
                LOG.debug("Found migration {}", migrationName);
                MigrationInfo migrationInfo = MigrationInfoHelper.extract(migrationName);
                Migration migration = ClassUtils.instantiate(migrationClass);
                resolvedMigrations.add(new JavaResolvedMigration(migrationInfo.getVersion(), migrationInfo.getDescription(), migration));
            }
        }
        Collections.sort(resolvedMigrations, Comparator.comparing(ResolvedMigration::getVersion));
        return resolvedMigrations;
    }
}
