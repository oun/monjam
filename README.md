# MonJam

MongoDB migration 

### Features
- Java based migration
- Run migration within transaction (MongoDB 4.0+)

### Installation

Add gradle plugin, dependency and configuration in your build.gradle.
```
plugins {
    id 'io.github.oun.monjam' version '0.1.1'
}

repositories {
    mavenCentral()
}

dependencies {
    compile 'io.github.oun:monjam-core:0.1.1'
}

monjam {
    url = 'mongodb://localhost:27017/?replicaSet=rs0'
    database = 'monjam'
    collection = 'schema_migrations'
    location = 'db/migration'
}
```

### Usage

#### Create Java Migration
```java
package db.migration;

import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;

public class V1__First_Migration implements Migration {
    @Override
    public void up(Context context) {
        // Run this when execute migrate
        MongoDatabase database = context.getDatabase();
        database.createCollection("my_collection");
        ...
    }

    @Override
    public void down(Context context) {
        // Run this when execute rollback
        database.createCollection("my_collection").drop();
        ...
    }
}
```

#### Migrate
```
./gradlew monjamMigrate
```

### Configuration
| name        | description                      | default |
|-------------|----------------------------------|---------|
| url         | Connection url                   | -       |
| database    | Database name                    | -       |
| collection  | Collection that store applied schema migrations | schema_migrations |
| location    | Schema migration files location  | db/migration |
