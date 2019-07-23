package com.monjam.core.rule;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class MongoRule implements TestRule {
    private static final MongodStarter starter = MongodStarter.getDefaultInstance();
    private MongodExecutable mongodExecutable;
    private MongodProcess mongodProcess;

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                startServer();
                try {
                    base.evaluate();
                } finally {
                    stopServer();
                }
            }
        };
    }

    private void startServer() throws Exception {
        mongodExecutable = starter.prepare(new MongodConfigBuilder()
                .version(Version.Main.LEGACY)
                .net(new Net("localhost", 12345, Network.localhostIsIPv6()))
                .build());
        mongodProcess = mongodExecutable.start();
    }

    private void stopServer() {
        mongodProcess.stop();
        mongodExecutable.stop();
    }
}
