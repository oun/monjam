package com.monjam.core.api;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MigrationVersionTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void constructor_GivenVersionString() {
        String version = "1.2.3";

        MigrationVersion migrationVersion = new MigrationVersion(version);

        assertThat(migrationVersion.getMajor(), equalTo(1));
        assertThat(migrationVersion.getMinor(), equalTo(2));
        assertThat(migrationVersion.getPatch(), equalTo(3));
    }

    @Test
    public void constructor_GivenVersionWithMajorAndMinor() {
        String version = "0.23";

        MigrationVersion migrationVersion = new MigrationVersion(version);

        assertThat(migrationVersion.getMajor(), equalTo(0));
        assertThat(migrationVersion.getMinor(), equalTo(23));
        assertThat(migrationVersion.getPatch(), equalTo(0));
    }

    @Test
    public void constructor_GivenVersionWithOnlyMajor() {
        String version = "12";

        MigrationVersion migrationVersion = new MigrationVersion(version);

        assertThat(migrationVersion.getMajor(), equalTo(12));
        assertThat(migrationVersion.getMinor(), equalTo(0));
        assertThat(migrationVersion.getPatch(), equalTo(0));
    }

    @Test
    public void constructor_GivenInvalidVersion() {
        String version = "a.1";

        exception.expect(MonJamException.class);

        new MigrationVersion(version);
    }

    @Test
    public void compareTo_NewerMajorVersion() {
        MigrationVersion version = new MigrationVersion("1.1.1");

        int result = version.compareTo(new MigrationVersion("2.0.0"));

        assertThat(result, equalTo(-1));
    }

    @Test
    public void compareTo_NewerMinorVersion() {
        MigrationVersion version = new MigrationVersion("1.1.1");

        int result = version.compareTo(new MigrationVersion("1.2.0"));

        assertThat(result, equalTo(-1));
    }

    @Test
    public void compareTo_NewerPatchVersion() {
        MigrationVersion version = new MigrationVersion("1.1.1");

        int result = version.compareTo(new MigrationVersion("1.1.10"));

        assertThat(result, equalTo(-1));
    }

    @Test
    public void compareTo_GivenOlderMajorVersion() {
        MigrationVersion version = new MigrationVersion("1.1.1");

        int result = version.compareTo(new MigrationVersion("0.9.0"));

        assertThat(result, equalTo(1));
    }

    @Test
    public void compareTo_GivenOlderMinorVersion() {
        MigrationVersion version = new MigrationVersion("1.1.1");

        int result = version.compareTo(new MigrationVersion("1.0.9"));

        assertThat(result, equalTo(1));
    }

    @Test
    public void compareTo_GivenOlderPatchVersion() {
        MigrationVersion version = new MigrationVersion("1.1.1");

        int result = version.compareTo(new MigrationVersion("1.1.0"));

        assertThat(result, equalTo(1));
    }

    @Test
    public void compareTo_GivenSameVersion() {
        MigrationVersion version = new MigrationVersion("1.2.3");

        int result = version.compareTo(new MigrationVersion("1.2.3"));

        assertThat(result, equalTo(0));
    }

    @Test
    public void equals_GivenSameVersion() {
        MigrationVersion version = new MigrationVersion("1.2.3");

        boolean result = version.equals(new MigrationVersion("1.2.3"));

        assertThat(result, equalTo(true));
    }

    @Test
    public void equals_GivenNewerVersion() {
        MigrationVersion version = new MigrationVersion("1.2.3");

        boolean result = version.equals(new MigrationVersion("2.2.3"));

        assertThat(result, equalTo(false));
    }

    @Test
    public void equals_GivenOlderVersion() {
        MigrationVersion version = new MigrationVersion("1.2.3");

        boolean result = version.equals(new MigrationVersion("1.2.0"));

        assertThat(result, equalTo(false));
    }

    @Test
    public void equals_GivenOtherObject() {
        MigrationVersion version = new MigrationVersion("1.2.3");

        boolean result = version.equals("1.2.3");

        assertThat(result, equalTo(false));
    }
}
