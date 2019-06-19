package com.monjam.core.executor;

import com.monjam.core.api.Context;

public interface MigrationExecutor {
    void executeUp(Context context);
    void executeDown(Context context);
}
