package com.monjam.core.configuration;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ConfigurationUtilsTest {
    private static final String PROPERTY_DATABASE = "monjam.database";
    private static final String PROPERTY_USERNAME = "monjam.username";
    private static final String PROPERTY_PASSWORD = "monjam.password";

    @Test
    public void populate_CopyFromHashMap() {
        Configuration configuration = new Configuration();
        configuration.setLocation("db/migration");
        configuration.setDatabase("temp");
        Map<String, String> map = new HashMap<>();
        map.put(PROPERTY_DATABASE, "testdb");
        map.put(PROPERTY_USERNAME, "admin");
        map.put(PROPERTY_PASSWORD, "1234");

        ConfigurationUtils.populate(configuration, map);

        assertThat(configuration.getLocation(), equalTo("db/migration"));
        assertThat(configuration.getDatabase(), equalTo("testdb"));
        assertThat(configuration.getUsername(), equalTo("admin"));
        assertThat(configuration.getPassword(), equalTo("1234"));
    }

    @Test
    public void populate_CopyFromHashMapWithUnknownProperty() {
        Configuration configuration = new Configuration();
        configuration.setLocation("db/migration");
        configuration.setDatabase("temp");
        Map<String, String> map = new HashMap<>();
        map.put("mamon.database", "testdb");

        ConfigurationUtils.populate(configuration, map);

        assertThat(configuration.getLocation(), equalTo("db/migration"));
        assertThat(configuration.getDatabase(), equalTo("temp"));
    }

    @Test
    public void populate_CopyFromObject() {
        Configuration configuration = new Configuration();
        configuration.setLocation("db/migration");
        configuration.setDatabase("temp");
        SourceProperties object = new SourceProperties();
        object.setUsername("admin");
        object.setPassword("1234");

        ConfigurationUtils.populate(configuration, object);

        assertThat(configuration.getLocation(), equalTo("db/migration"));
        assertThat(configuration.getDatabase(), equalTo("temp"));
        assertThat(configuration.getUsername(), equalTo("admin"));
        assertThat(configuration.getPassword(), equalTo("1234"));
    }

    @Test
    public void populate_CopyFromObjectWithNullValue() {
        Configuration configuration = new Configuration();
        configuration.setLocation("db/migration");
        configuration.setDatabase("temp");
        configuration.setUsername("admin");
        SourceProperties object = new SourceProperties();
        object.setUsername(null);
        object.setPassword(null);

        ConfigurationUtils.populate(configuration, object);

        assertThat(configuration.getLocation(), equalTo("db/migration"));
        assertThat(configuration.getDatabase(), equalTo("temp"));
        assertThat(configuration.getUsername(), equalTo("admin"));
        assertThat(configuration.getPassword(), is(nullValue()));
    }

    static class SourceProperties {
        private String username;
        private String password;
        private String unknown;

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

        public String getUnknown() {
            return unknown;
        }

        public void setUnknown(String unknown) {
            this.unknown = unknown;
        }

        public String getFakeProperty(String param) {
            return null;
        }
    }
}
