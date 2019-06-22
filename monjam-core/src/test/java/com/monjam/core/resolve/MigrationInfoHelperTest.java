package com.monjam.core.resolve;

import com.monjam.core.api.MigrationVersion;
import com.monjam.core.api.MonJamException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MigrationInfoHelperTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void extract_GivenVersionAndDescription() {
        String migrationName = "V1_2_3__Hello_monjam";

        MigrationInfo migrationInfo = MigrationInfoHelper.extract(migrationName);

        assertThat(migrationInfo.getVersion(), equalTo(new MigrationVersion("1.2.3")));
        assertThat(migrationInfo.getDescription(), equalTo("Hello monjam"));
    }

    @Test
    public void extract_GivenVersionAndBlankDescription() {
        String migrationName = "V1_2_3__";

        MigrationInfo migrationInfo = MigrationInfoHelper.extract(migrationName);

        assertThat(migrationInfo.getVersion(), equalTo(new MigrationVersion("1.2.3")));
        assertThat(migrationInfo.getDescription(), equalTo(""));
    }

    @Test
    public void extract_GivenVersion() {
        String migrationName = "V1_2_3";

        MigrationInfo migrationInfo = MigrationInfoHelper.extract(migrationName);

        assertThat(migrationInfo.getVersion(), equalTo(new MigrationVersion("1.2.3")));
        assertThat(migrationInfo.getDescription(), equalTo(""));
    }

    @Test
    public void extract_GivenMajorVersion() {
        String migrationName = "V1__First_version";

        MigrationInfo migrationInfo = MigrationInfoHelper.extract(migrationName);

        assertThat(migrationInfo.getVersion(), equalTo(new MigrationVersion("1.0.0")));
        assertThat(migrationInfo.getDescription(), equalTo("First version"));
    }

    @Test
    public void extract_GivenMajorAndMinorVersion() {
        String migrationName = "V1_1__Change_minor_version";

        MigrationInfo migrationInfo = MigrationInfoHelper.extract(migrationName);

        assertThat(migrationInfo.getVersion(), equalTo(new MigrationVersion("1.1.0")));
        assertThat(migrationInfo.getDescription(), equalTo("Change minor version"));
    }

    @Test
    public void extract_WithoutPrefix() {
        String migrationName = "1_1__Should_have_prefix";

        exception.expect(MonJamException.class);
        exception.expectMessage("Migration version should have prefix 'V'");

        MigrationInfoHelper.extract(migrationName);
    }

    @Test
    public void extract_WithoutVersion() {
        String migrationName = "Hello_world";

        exception.expect(MonJamException.class);
        exception.expectMessage("Migration version should have prefix 'V'");

        MigrationInfoHelper.extract(migrationName);
    }
}
