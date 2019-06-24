package com.monjam.gradle;

import com.monjam.gradle.task.MigrateTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MonjamPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create("monjam", MonjamExtension.class);
        project.getTasks().create("mjMigrate", MigrateTask.class);
    }
}
