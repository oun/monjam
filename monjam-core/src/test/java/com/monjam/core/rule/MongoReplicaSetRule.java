package com.monjam.core.rule;

import com.mongodb.BasicDBList;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.bson.Document;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MongoReplicaSetRule implements TestRule {
    private static final MongodStarter starter = MongodStarter.getDefaultInstance();
    private int mongodNodes = 2;
    private List<Integer> mongodPorts = Arrays.asList(27117, 27118);
    private List<MongodExecutable> mongodExecutables = new ArrayList<>();
    private List<MongodProcess> mongodProcesses = new ArrayList<>();

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                startServer();
                initializeReplicaSet();
                try {
                    base.evaluate();
                } finally {
                    stopServer();
                }
            }
        };
    }

    private void startServer() throws Exception {
        for (int i = 0; i < mongodNodes; i++) {
            MongodExecutable mongodExecutable = starter.prepare(new MongodConfigBuilder()
                    .version(Version.Main.PRODUCTION)
                    .withLaunchArgument("--replSet", "rs0")
                    .cmdOptions(new MongoCmdOptionsBuilder().useNoJournal(false).build())
                    .net(new Net("localhost", mongodPorts.get(i), Network.localhostIsIPv6()))
                    .build());
            MongodProcess mongodProcess = mongodExecutable.start();
            mongodExecutables.add(mongodExecutable);
            mongodProcesses.add(mongodProcess);
        }
    }

    private void initializeReplicaSet() {
        try (MongoClient mongoClient = MongoClients.create(String.format("mongodb://localhost:%d", mongodPorts.get(0)))) {
            MongoDatabase adminDatabase = mongoClient.getDatabase("admin");
            Document config = new Document("_id", "rs0");
            BasicDBList members = new BasicDBList();
            for (int i = 0; i < mongodNodes; i++) {
                members.add(new Document("_id", i).append("host", "localhost:" + mongodPorts.get(i)));
            }
            config.put("members", members);
            adminDatabase.runCommand(new Document("replSetInitiate", config));
        }
    }

    private void stopServer() {
        for (int i = 0; i < mongodNodes; i++) {
            mongodProcesses.get(i).stop();
            mongodExecutables.get(i).stop();
        }
    }
}
