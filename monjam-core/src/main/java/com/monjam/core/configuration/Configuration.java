package com.monjam.core.configuration;

public class Configuration {
    private ClassLoader classLoader;
    private String collection = "schema_migrations";
    private String location = "db/migrations";
    private String url;
    private String username;
    private String password;
    private String authDatabase = "admin";
    private String database;
    private String scriptMigrationExtension = "js";

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthDatabase() {
        return authDatabase;
    }

    public void setAuthDatabase(String authDatabase) {
        this.authDatabase = authDatabase;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getScriptMigrationExtension() {
        return scriptMigrationExtension;
    }

    public void setScriptMigrationExtension(String scriptMigrationExtension) {
        this.scriptMigrationExtension = scriptMigrationExtension;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("collection = ").append(collection)
                .append(", location = ").append(location)
                .append(", url = ").append(url)
                .append(", username = ").append(username)
                .append(", password = ").append(password)
                .append(", database = ").append(database)
                .append(", authDatabase = ").append(authDatabase)
                .append(", scriptMigrationExtension = ").append(scriptMigrationExtension)
                .toString();
    }
}
