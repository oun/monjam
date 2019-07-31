package com.monjam.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MigrationVersion implements Comparable<MigrationVersion> {
    private static final Pattern PATTERN = Pattern.compile("(\\d+)(\\.\\d+)?(\\.\\d+)?");
    private List<Integer> versionParts;

    public MigrationVersion(String version) {
        Matcher matcher = PATTERN.matcher(version);
        if (!matcher.matches()) {
            throw new MonJamException("Invalid version. Version must be [number].[number].[number]");
        }
        versionParts = new ArrayList<>();
        for (String part : version.split("\\.")) {
            versionParts.add(Integer.parseInt(part));
        }
        for (int i = versionParts.size(); i <= 3; i++) {
            versionParts.add(0);
        }
    }

    public Integer getMajor() {
        return versionParts.get(0);
    }

    public Integer getMinor() {
        return versionParts.get(1);
    }

    public Integer getPatch() {
        return versionParts.get(2);
    }

    @Override
    public int compareTo(MigrationVersion o) {
        int result = 0;
        for (int i = 0; i < 3; i++) {
            Integer thisPart = this.versionParts.get(i);
            Integer otherPart = o.versionParts.get(i);
            if (!thisPart.equals(otherPart)) {
                result = thisPart.compareTo(otherPart);
                break;
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (int i = 0; i < 3; i++) {
            result = 31 * result + versionParts.get(i).hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MigrationVersion)) {
            return false;
        }
        MigrationVersion other = (MigrationVersion) o;
        return this.compareTo(other) == 0;
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d", getMajor(), getMinor(), getPatch());
    }
}
