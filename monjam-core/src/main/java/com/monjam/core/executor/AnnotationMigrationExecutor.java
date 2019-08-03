package com.monjam.core.executor;

import com.monjam.core.api.Context;
import com.monjam.core.api.MonJamException;

import java.lang.reflect.Method;

public class AnnotationMigrationExecutor implements MigrationExecutor {
    private final Method method;
    private final Object instance;

    public AnnotationMigrationExecutor(Method method, Object instance) {
        this.method = method;
        this.instance = instance;
    }

    @Override
    public void execute(Context context) {
        if (method.getParameterCount() != 1) {
            throw new MonJamException("Migration annotation methods must have exactly one parameter");
        }
        if (!Context.class.isAssignableFrom(method.getParameterTypes()[0])) {
            throw new MonJamException("Migration annotation methods must have Context parameter");
        }
        try {
            method.invoke(instance, context);
        } catch (Exception e) {
            throw new MonJamException("Error invoked migration method" + method.getName(), e);
        }
    }
}
