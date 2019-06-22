package com.monjam.core;

import com.monjam.core.api.Configuration;
import com.monjam.core.command.Command;
import com.monjam.core.command.DbMigrate;

public class Monjam {
    private final Configuration configuration;

    public Monjam(Configuration configuration) {
        this.configuration = configuration;
    }

    public void migrate() {
        Command dbMigrate = new DbMigrate(configuration);
        dbMigrate.execute();
    }
}
