package com.monjam.core.executor;

import com.monjam.core.api.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoopMigrationExecutor implements MigrationExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(NoopMigrationExecutor.class);

    @Override
    public void execute(Context context) {
        LOG.debug("Execute no operation");
    }
}
