package com.monjam.core.executor;

import com.monjam.core.api.Context;
import com.monjam.core.api.MonJamException;
import com.monjam.core.database.DbTemplate;

public class ScriptMigrationExecutor implements MigrationExecutor {
    private String script;

    public ScriptMigrationExecutor(String script) {
        this.script = script;
    }

    @Override
    public void execute(Context context) {
        try {
            DbTemplate template = new DbTemplate(context.getClient(), context.getSession(), context.getConfiguration().getDatabase());
            template.executeCommand(script);
        } catch (Exception e) {
            throw new MonJamException("Error executing script", e);
        }
    }
}
