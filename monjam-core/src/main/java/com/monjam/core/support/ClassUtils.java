package com.monjam.core.support;

import com.monjam.core.api.MonJamException;

public class ClassUtils {
    private ClassUtils() {
        throw new IllegalStateException("This class should not instantiate");
    }

    public static final <T> T instantiate(Class<?> clazz) {
        try {
            return (T) clazz.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new MonJamException("Unable to instantiate class " + clazz.getName());
        }
    }
}
