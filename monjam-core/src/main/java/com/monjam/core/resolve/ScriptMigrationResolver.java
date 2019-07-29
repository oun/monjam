package com.monjam.core.resolve;

import com.monjam.core.api.MigrationType;
import com.monjam.core.api.MonJamException;
import com.monjam.core.configuration.Configuration;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScriptMigrationResolver implements MigrationResolver {
    private static final Logger LOG = LoggerFactory.getLogger(ScriptMigrationResolver.class);

    private Configuration configuration;

    public ScriptMigrationResolver(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public List<ResolvedMigration> resolveMigrations(MigrationType type) {
        if (configuration.getLocation() == null) {
            throw new MonJamException("Missing migration location");
        }
        LOG.info("Scanning migration scripts in {}", configuration.getLocation());
        String[] locations = Arrays.stream(configuration.getLocation().split(","))
                .map(String::trim)
                .toArray(String[]::new);
        List<ResolvedMigration> resolvedMigrations = new ArrayList<>();
        try(ScanResult scanResult = new ClassGraph()
                .addClassLoader(configuration.getClassLoader())
                .whitelistPaths(locations)
                .scan()
        ) {
            for (Resource resource : scanResult.getResourcesWithExtension(configuration.getScriptMigrationExtension())) {
                String migrationName = convertPathToMigrationName(resource.getPath());
                MigrationInfo migrationInfo = MigrationInfoHelper.extract(migrationName);
                if (migrationInfo.getType() != type) {
                    continue;
                }
                LOG.debug("Found migration script {}", migrationName);
                resolvedMigrations.add(new ScriptResolvedMigration(migrationInfo.getVersion(), migrationInfo.getDescription(), resource.getContentAsString()));
            }
        } catch (IOException e) {
            throw new MonJamException("Error reading script file", e);
        }
        Collections.sort(resolvedMigrations, Comparator.comparing(ResolvedMigration::getVersion));
        return resolvedMigrations;
    }

    private String convertPathToMigrationName(String path) {
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        String extension = fileName.substring(fileName.lastIndexOf("."));
        return fileName.substring(0, fileName.length() - extension.length());
    }
}
