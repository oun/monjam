package com.monjam.gradle.task;

import com.monjam.core.Monjam;

public class MigrateTask extends AbstractMonjamTask {
    public MigrateTask() {
        super();
        setDescription("Migrate schema to latest version.");
    }

    @Override
    protected void run(Monjam monjam) {
        monjam.migrate();
    }
}
