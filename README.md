# kotlin-encryption

encryption utilities

## Build

Local publication:
```shell
./gradlew clean signMavenPublication publishToMavenLocal
```

See `~/.m2/repository/dev/mbo/kotlin-encryption` for the created content

Use the Release Plugin to release a version.

## Release

All releases have to be done from main branch. A release also triggers the upload to maven central.

The following example releases version 1.0.0 and prepares a development version of 1.0.1-SNAPSHOT.

```shell
./gradlew release -Prelease.useAutomaticVersion=true \
  -Prelease.releaseVersion=1.0.0 \
  -Prelease.newVersion=1.0.1-SNAPSHOT
```

You can skip the version definitions to release the snapshot and automatically increment.

```shell
./gradlew release -Prelease.useAutomaticVersion=true
```
