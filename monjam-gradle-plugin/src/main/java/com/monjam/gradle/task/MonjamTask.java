package com.monjam.gradle.task;

import com.monjam.core.Monjam;
import com.monjam.core.api.Configuration;
import com.monjam.gradle.MonjamExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public abstract class MonjamTask extends DefaultTask {
    private MonjamExtension extension;

    public MonjamTask() {
        super();
        extension = (MonjamExtension) getProject().getExtensions().getByName("monjam");
    }

    protected abstract void run(Monjam monjam);

    @TaskAction
    public void run() {
        Configuration configuration = Configuration.builder()
                .url(extension.getUrl())
                .location(extension.getLocation())
                .collection(extension.getCollection())
                .database(extension.getDatabase())
                .build();
        run(new Monjam(configuration));
    }
}
