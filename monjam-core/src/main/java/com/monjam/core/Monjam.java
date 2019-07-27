package com.monjam.core;

import com.monjam.core.configuration.Configuration;
import com.monjam.core.command.Command;
import com.monjam.core.command.DbMigrate;
import com.monjam.core.command.DbRollback;

public class Monjam {
    private final Configuration configuration;

    public Monjam(Configuration configuration) {
        this.configuration = configuration;
    }

    public void migrate() {
        Command dbMigrate = new DbMigrate(configuration);
        dbMigrate.execute();
    }

    public void rollback() {
        Command dbRollback = new DbRollback(configuration);
        dbRollback.execute();
    }
}
