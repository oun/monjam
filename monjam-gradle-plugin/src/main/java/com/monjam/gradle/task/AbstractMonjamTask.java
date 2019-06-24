package com.monjam.gradle.task;

import com.monjam.core.Monjam;
import com.monjam.core.api.Configuration;
import com.monjam.core.api.MonJamException;
import com.monjam.gradle.MonjamExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractMonjamTask extends DefaultTask {
    private MonjamExtension extension;

    public AbstractMonjamTask() {
        super();
        setGroup("Monjam");
        extension = (MonjamExtension) getProject().getExtensions().getByName("monjam");
    }

    protected abstract void run(Monjam monjam);

    @TaskAction
    public void run() {
        try {
            Set<URL> urls = new HashSet<>();
            addClassesAndResourcesDirs(urls);

            ClassLoader classLoader = new URLClassLoader(
                    urls.toArray(new URL[0]),
                    getProject().getBuildscript().getClassLoader());
            Configuration configuration = Configuration.builder()
                    .classLoader(classLoader)
                    .url(extension.getUrl())
                    .location(extension.getLocation())
                    .collection(extension.getCollection())
                    .database(extension.getDatabase())
                    .build();
            run(new Monjam(configuration));
        } catch (Exception e) {
            throw new MonJamException("Error occurred while executing " + getName(), e);
        }
    }

    private void addClassesAndResourcesDirs(Set<URL> extraURLs) throws Exception {
        JavaPluginConvention plugin = getProject().getConvention().getPlugin(JavaPluginConvention.class);

        for (SourceSet sourceSet : plugin.getSourceSets()) {
            @SuppressWarnings("JavaReflectionMemberAccess")
            Method getClassesDirs = SourceSetOutput.class.getMethod("getClassesDirs");

            // use alternative method available in Gradle 4.0
            FileCollection classesDirs = (FileCollection) getClassesDirs.invoke(sourceSet.getOutput());
            for (File directory : classesDirs.getFiles()) {
                URL classesUrl = directory.toURI().toURL();
                getLogger().debug("Adding directory to Classpath: " + classesUrl);
                extraURLs.add(classesUrl);
            }

            URL resourcesUrl = sourceSet.getOutput().getResourcesDir().toURI().toURL();
            getLogger().debug("Adding directory to Classpath: " + resourcesUrl);
            extraURLs.add(resourcesUrl);
        }
    }
}
