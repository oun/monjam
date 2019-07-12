package com.monjam.gradle.task;

import com.monjam.core.Monjam;

public class RollbackTask extends AbstractMonjamTask {
    public RollbackTask() {
        super();
        setDescription("Rollback most recent applied migration version.");
    }

    @Override
    protected void run(Monjam monjam) {
        monjam.rollback();
    }
}
