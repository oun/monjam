package com.monjam.core.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigurationUtils {
    private static final String CONFIG_PREFIX = "monjam.";

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationUtils.class);

    private ConfigurationUtils() {
        throw new IllegalStateException("This class should not instantiate");
    }

    public static void populate(Configuration configuration, Map<String, ?> properties) {
        try {
            for (Map.Entry<String, ?> entry : properties.entrySet()) {
                if (entry.getKey().startsWith(CONFIG_PREFIX)) {
                    String property = entry.getKey().replace(CONFIG_PREFIX, "");
                    setPropertyValue(configuration, property, entry.getValue());
                }
            }
        } catch (Exception e) {
            LOG.error("Error while populating configuration", e);
        }
    }

    public static void populate(Configuration configuration, Object source) {
        try {
            List<String> properties = configurationProperties(configuration);
            for (Method method : source.getClass().getDeclaredMethods()) {
                if (!(method.getName().startsWith("get") && method.getParameterCount() == 0)) {
                    continue;
                }
                String property = method.getName().substring(3);
                property = property.substring(0, 1).toLowerCase() + property.substring(1);
                if (!properties.contains(property)) {
                    continue;
                }
                Object sourceValue = method.invoke(source);
                if (sourceValue == null) {
                    continue;
                }
                setPropertyValue(configuration, property, sourceValue);
            }
        } catch (Exception e) {
            LOG.error("Error while populating configuration", e);
        }
    }

    private static List<String> configurationProperties(Configuration configuration) {
        return Arrays.stream(configuration.getClass().getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    private static void setPropertyValue(Object object, String property, Object value) {
        try {
            String methodName = propertySetterMethod(property);
            Method method = object.getClass().getDeclaredMethod(methodName, value.getClass());
            method.invoke(object, value);
        } catch (Exception e) {
            LOG.warn("Could not set configuration property {} with value {}", property, value, e);
        }
    }

    private static String propertySetterMethod(String property) {
        return "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
    }
}
