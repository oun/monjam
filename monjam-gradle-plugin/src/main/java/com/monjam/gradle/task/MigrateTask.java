package com.monjam.gradle.task;

import com.monjam.core.Monjam;

public class MigrateTask extends MonjamTask {
    @Override
    protected void run(Monjam monjam) {
        monjam.migrate();
    }
}
