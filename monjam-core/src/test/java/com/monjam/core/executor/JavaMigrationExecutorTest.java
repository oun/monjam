package com.monjam.core.executor;

import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;
import com.monjam.core.api.MigrationType;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JavaMigrationExecutorTest {
    @Test
    public void execute_GivenMigrationTypeMigrate() {
        Migration migration = mock(Migration.class);
        MigrationExecutor executor = new JavaMigrationExecutor(migration);
        Context context = createContext(MigrationType.MIGRATE);

        executor.execute(context);

        verify(migration, times(1)).up(eq(context));
    }

    @Test
    public void execute_GivenMigrationTypeRollback() {
        Migration migration = mock(Migration.class);
        MigrationExecutor executor = new JavaMigrationExecutor(migration);
        Context context = createContext(MigrationType.ROLLBACK);

        executor.execute(context);

        verify(migration, times(1)).down(eq(context));
    }

    private Context createContext(MigrationType type) {
        Context context = new Context();
        context.setMigrationType(type);
        return context;
    }
}
