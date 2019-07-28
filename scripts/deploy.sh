#!/usr/bin/env bash

# openssl aes-256-cbc -K $encrypted_e07e39bfdc1d_key -iv $encrypted_e07e39bfdc1d_iv -in scripts/travis.gpg.enc -out travis.gpg -d
./gradlew monjam-core:publish -PsonatypeUsername=${SONATYPE_USERNAME} -PsonatypePassword=${SONATYPE_PASSWORD} -Psigning.keyId=${GPG_KEY_ID} -Psigning.password=${GPG_KEY_PASSPHRASE} -Psigning.secretKeyRingFile=travis.gpg
./gradlew monjam-gradle-plugin:publishPlugins -Pgradle.publish.key=$GRADLE_KEY -Pgradle.publish.secret=$GRADLE_SECRET
