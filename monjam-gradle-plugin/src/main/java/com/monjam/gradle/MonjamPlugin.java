package com.monjam.gradle;

import com.monjam.gradle.task.MigrateTask;
import com.monjam.gradle.task.RollbackTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MonjamPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create("monjam", MonjamExtension.class);
        project.getTasks().create("monjamMigrate", MigrateTask.class);
        project.getTasks().create("monjamRollback", RollbackTask.class);
    }
}
