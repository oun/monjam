package com.monjam.core.resolve;

import com.monjam.core.annotation.Migrate;
import com.monjam.core.annotation.MongoMigration;
import com.monjam.core.api.MigrationType;
import com.monjam.core.api.MonJamException;
import com.monjam.core.configuration.Configuration;
import com.monjam.core.executor.AnnotationMigrationExecutor;
import com.monjam.core.support.ClassUtils;
import io.github.classgraph.AnnotationEnumValue;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationMigrationResolver implements MigrationResolver {
    private static final Logger LOG = LoggerFactory.getLogger(AnnotationMigrationResolver.class);
    private static final String VERSION = "version";
    private static final String DESCRIPTION = "description";
    private static final String TYPE = "type";

    private final Configuration configuration;

    public AnnotationMigrationResolver(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public List<ResolvedMigration> resolveMigrations(MigrationType type) {
        if (configuration.getLocation() == null) {
            throw new MonJamException("Missing migration location");
        }
        LOG.info("Scanning annotated classes in {}", configuration.getLocation());
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
            Map<String, Object> instanceCache = new HashMap<>();
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(MongoMigration.class.getName())) {
                for (MethodInfo methodInfo : classInfo.getMethodInfo()) {
                    if (!methodInfo.hasAnnotation(Migrate.class.getName())) {
                        continue;
                    }
                    AnnotationInfo annotationInfo = methodInfo.getAnnotationInfo(Migrate.class.getName());
                    MigrationInfo migrationInfo = extractMigrationInfo(annotationInfo);
                    if (migrationInfo.getType() != type) {
                        continue;
                    }
                    if (!instanceCache.containsKey(classInfo.getName())) {
                        instanceCache.put(classInfo.getName(), ClassUtils.instantiate(classInfo.loadClass()));
                    }
                    Object instance = instanceCache.get(classInfo.getName());
                    AnnotationMigrationExecutor executor = new AnnotationMigrationExecutor(methodInfo.loadClassAndGetMethod(), instance);
                    resolvedMigrations.add(new AnnotationResolvedMigration(migrationInfo.getVersion(), migrationInfo.getDescription(), executor));
                }
            }
        }
        Collections.sort(resolvedMigrations, Comparator.comparing(ResolvedMigration::getVersion));
        return resolvedMigrations;
    }

    private MigrationInfo extractMigrationInfo(AnnotationInfo annotationInfo) {
        AnnotationParameterValueList parameterValues = annotationInfo.getParameterValues();
        String version = (String) parameterValues.getValue(VERSION);
        String description = (String) parameterValues.getValue(DESCRIPTION);
        MigrationType type = (MigrationType) ((AnnotationEnumValue) parameterValues.getValue(TYPE)).loadClassAndReturnEnumValue();
        return new MigrationInfo(version, type, description);
    }
}
