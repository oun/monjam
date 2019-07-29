# MonJam [![Build Status](https://travis-ci.com/oun/monjam.svg?branch=master)](https://travis-ci.com/oun/monjam) [![codecov](https://codecov.io/gh/oun/monjam/branch/master/graph/badge.svg)](https://codecov.io/gh/oun/monjam) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=oun_monjam&metric=alert_status)](https://sonarcloud.io/dashboard?id=oun_monjam)

MongoDB migration

### Features
- Gradle plugin
- Java and script migration
- Multi-document transaction (MongoDB 4.0+)

### Road Map
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
        MongoCollection collection = context.getDatabase().getCollection("users");
        collection.updateMany(context.getSession(), eq("prefix", "Mr."), set("prefix", 1));
        collection.updateMany(context.getSession(), eq("prefix", "Mrs."), set("prefix", 2));
    }

    @Override
    public void down(Context context) {
        // Execute on rollback
        MongoCollection collection = context.getDatabase().getCollection("users");
        collection.updateMany(context.getSession(), eq("prefix", 1), set("prefix", "Mr."));
        collection.updateMany(context.getSession(), eq("prefix", 2), set("prefix", "Mrs."));
    }
}
```

##### Script Migration

Create migrate script named V1_0_0__Change_user_prefix_type.js

```javascript
db.users.update({prefix: 'Mr.'}, {$set: {prefix: 1}}, {multi: true});
db.users.update({prefix: 'Mrs.'}, {$set: {prefix: 2}}, {multi: true});
```

Create rollback script named U1_0_0__Revert_change_user_prefix_type.js

```javascript
db.users.update({prefix: 1}, {$set: {prefix: 'Mr.'}}, {multi: true});
db.users.update({prefix: 2}, {$set: {prefix: 'Mrs.'}}, {multi: true});
```

##### File Name Pattern
{Prefix}{Version}__{Description}
- Prefix: V for migrate, U for rollback (applicable to script migration)
- Version: Sem-ver format separated each part with underscored
- Separator: Two underscores
- Description: Underscores separated words

#### Execute Migrate
`./gradlew monjamMigrate`

As each migration get applied, the schema migration history collection (default to schema_migrations ) is updated with each document corresponding to applied migration
```
{
    "_id" : ObjectId("5d3bbedb93b76e755467566d"),
    "version" : "1.0.0",
    "description" : "Change user prefix type",
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
| scriptMigrationExtension | Script migration file extension | js |
