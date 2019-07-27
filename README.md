# MonJam [![Build Status](https://travis-ci.com/oun/monjam.svg?branch=master)](https://travis-ci.com/oun/monjam) [![codecov](https://codecov.io/gh/oun/monjam/branch/master/graph/badge.svg)](https://codecov.io/gh/oun/monjam)

MongoDB migration

### Features
- Gradle plugin
- Java based migration
- Run migration within transaction (MongoDB 4.0+)

### Road Map
- JS script based migration
- Maven plugin

### Installation

Add gradle plugin, dependency and configuration in your build.gradle.
```
plugins {
    id 'io.github.oun.monjam' version '0.2.0'
}

repositories {
    mavenCentral()
}

dependencies {
    compile 'io.github.oun:monjam-core:0.2.0'
}

monjam {
    url = 'mongodb://localhost:27017/?replicaSet=rs0'
    database = 'monjam'
    collection = 'schema_migrations'
    location = 'db/migration'
}
```

### Usage

#### Create Migration

##### Java Migration

```java
package db.migration;

import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;

public class V1_0_0__First_Migration implements Migration {
    @Override
    public void up(Context context) {
        // Execute migrate
        MongoDatabase database = context.getDatabase();
        database.createCollection("my_collection");
    }

    @Override
    public void down(Context context) {
        // Execute rollback
        database.createCollection("my_collection").drop();
    }
}
```

##### File Name Pattern
{Prefix}{Version}__{Description}
- Prefix: V for versioned migration
- Version: Sem-ver format separated each part with underscored
- Separator: two underscores
- Description: Underscores separated words

#### Execute Migrate
`./gradlew monjamMigrate`

#### Execute Rollback
`./gradlew monjamRollback`

### Configuration

Configuration can be defined in build.gradle.
```
monjam {
    url = 'mongodb://localhost:27017/?replicaSet=rs0'
    database = 'monjam'
    collection = 'schema_migrations'
    location = 'db/migration'
}
```
or using gradle properties passed directly via command-line.
`./gradlew monjamMigrate -Pmonjam.username=admin -Pmonjam.password=secret`

| name        | description                      | default |
|-------------|----------------------------------|---------|
| url         | Connection url                   | -       |
| database    | Database name                    | -       |
| username    | Username                         | -       |
| password    | Password                         | -       |
| authDatabase | Authentication database name    | admin   |
| collection  | Collection that store applied schema migrations | schema_migrations |
| location    | Schema migration files location  | db/migration |
