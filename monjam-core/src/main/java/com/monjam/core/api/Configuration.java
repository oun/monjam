package com.monjam.core.api;

public class Configuration {
    private ClassLoader classLoader;
    private String collection = "schema_migrations";
    private String location = "db/migrations";
    private String url;
    private String username;
    private String password;
    private String database;

    public static ConfigurationBuilder builder() {
        return new ConfigurationBuilder();
    }

    public String getCollection() {
        return collection;
    }

    public String getLocation() {
        return location;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public static class ConfigurationBuilder {
        private Configuration configuration = new Configuration();

        public ConfigurationBuilder classLoader(ClassLoader classLoader) {
            configuration.classLoader = classLoader;
            return this;
        }

        public ConfigurationBuilder location(String location) {
            configuration.location = location;
            return this;
        }

        public ConfigurationBuilder collection(String collection) {
            configuration.collection = collection;
            return this;
        }

        public ConfigurationBuilder database(String database) {
            configuration.database = database;
            return this;
        }

        public ConfigurationBuilder url(String url) {
            configuration.url = url;
            return this;
        }

        public ConfigurationBuilder username(String username) {
            configuration.username = username;
            return this;
        }

        public ConfigurationBuilder password(String password) {
            configuration.password = password;
            return this;
        }

        public Configuration build() {
            return configuration;
        }
    }
}
