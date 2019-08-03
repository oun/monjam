# MonJam [![Build Status](https://travis-ci.com/oun/monjam.svg?branch=master)](https://travis-ci.com/oun/monjam) [![codecov](https://codecov.io/gh/oun/monjam/branch/master/graph/badge.svg)](https://codecov.io/gh/oun/monjam) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=oun_monjam&metric=alert_status)](https://sonarcloud.io/dashboard?id=oun_monjam)

MongoDB migration

## Features
- Gradle plugin
- Java and script migration
- Multi-document transaction (MongoDB 4.0+)

## Road Map
- Integration with Spring MongoTemplate
- Maven plugin
- Validate, info, clean command

## Installation

Add gradle plugin, dependency and configuration in your build.gradle.
```
plugins {
    id 'io.github.oun.monjam' version '0.4.0'
}

repositories {
    mavenCentral()
}

dependencies {
    compile 'io.github.oun:monjam-core:0.4.0'
}

monjam {
    url = 'mongodb://localhost:27017/?replicaSet=rs0'
    database = 'monjam'
    collection = 'schema_migrations'
    location = 'db/migration'
}
```

See the [code example](https://github.com/oun/monjam-example) for Spring-based application.

## Usage

### Create Migration

Annotated class with @MongoMigration annotation and each methods with @Migrate annotation. Method with annotation parameter type MIGRATE and ROLLBACK will be executed on migrate and rollback respectively.

#### Annotation based Java Migration
```java
package db.migration;

import com.monjam.core.annotation.Migrate;
import com.monjam.core.annotation.MongoMigration;
import com.monjam.core.api.Context;
import com.monjam.core.api.MigrationType;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

@MongoMigration
public class UserMigration {
    @Migrate(type = MigrationType.MIGRATE, version = "1.0.0", description = "Change user prefix type")
    public void changeUserPrefixType(Context context) {
        // Execute on migrate version 1.0.0
    }

    @Migrate(type = MigrationType.ROLLBACK, version = "1.0.0", description = "Revert user prefix type")
    public void revertChangeUserPrefixType(Context context) {
        // Execute on rollback version 1.0.0
    }
}
```

#### Java Migration

Create java based migration class implementing Migration interface. The up and down method will be executed upon running migrate and rollback command respectively.

```java
package db.migration;

import com.mongodb.client.MongoCollection;
import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class V1_0_0__Change_user_prefix_type implements Migration {
    @Override
    public void up(Context context) {
        // Execute on migrate
    }

    @Override
    public void down(Context context) {
        // Execute on rollback
    }
}
```

#### Script Migration

Create migrate script V1_0_0__Change_user_prefix_type.js

```javascript
db.users.update({prefix: 'Mr.'}, {$set: {prefix: 1}}, {multi: true});
db.users.update({prefix: 'Mrs.'}, {$set: {prefix: 2}}, {multi: true});
```

#### File Name Pattern
{Prefix}{Version}__{Description}
- Prefix: V for migrate, U for rollback (applicable to script migration)
- Version: Sem-ver format separated each part with underscored
- Separator: Two underscores
- Description: Underscores separated words

### Execute Migrate
`./gradlew monjamMigrate`

As each migration get applied, the schema migration history collection (default to schema_migrations) is updated with each document corresponding to applied migration

| _id | version | description | executedAt |
|-----|---------|-------------|------------|
| 5d3bbedb93b76e755467566d | 1.0.0 | Change user prefix type | 2019-07-27T03:02:51.555Z |

## Command

### Migrate

`gradle monjamMigrate`

Migrates database to the latest version. Monjam will create schema migration history collection automatically if it does not exists.

### Rollback

`gradle monjamRollback`

Rollback the most recently applied migration.

## Configuration

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

| Name        | Description                      | Default |
|-------------|----------------------------------|---------|
| url         | Connection url                   | -       |
| database    | Database name                    | -       |
| username    | Username                         | -       |
| password    | Password                         | -       |
| authDatabase | Authentication database name    | admin   |
| collection  | Collection that store applied schema migrations | schema_migrations |
| location    | Schema migration files locations | db/migration |
| target      | Target version to migrate or rollback. For migrate, migration with version higher will be ignored. For rollback, migration with version equals or lower will be ignored | latest version (migrate), previous version (rollback) |
| scriptMigrationExtension | Script migration file extension | js |
