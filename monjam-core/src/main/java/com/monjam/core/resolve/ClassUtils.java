package com.monjam.core.resolve;

import com.monjam.core.api.MonJamException;

public class ClassUtils {
    public static final <T> T instantiate(Class<?> clazz) {
        try {
            return (T) clazz.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new MonJamException("Unable to instantiate class " + clazz.getName());
        }
    }
}
