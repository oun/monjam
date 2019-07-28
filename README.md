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
    id 'io.github.oun.monjam' version '0.3.0'
}

repositories {
    mavenCentral()
}

dependencies {
    compile 'io.github.oun:monjam-core:0.3.0'
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

public class V1_0_0__Create_collection implements Migration {
    @Override
    public void up(Context context) {
        // Execute on migrate
        MongoDatabase database = context.getDatabase();
        database.createCollection("my_collection");
    }

    @Override
    public void down(Context context) {
        // Execute on rollback
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

As each migration get applied, the schema migration history collection (default to schema_migrations ) is updated with each document corresponding to applied migration
```json
{
    "_id" : ObjectId("5d3bbedb93b76e755467566d"),
    "version" : "1.0.0",
    "description" : "Create collection",
    "executedAt" : ISODate("2019-07-27T03:02:51.555Z")
}
```

### Command

#### Migrate

Mirates database to the latest version. Monjam will create schema migration history collection automatically if it does not exists.

#### Rollback

Rollback the most recently applied migration.

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
