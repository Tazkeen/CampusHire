#!/bin/sh
# Gradle startup script for Unix-like environments, including GitHub Actions.

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P) || exit 1

if [ -n "$JAVA_HOME" ]; then
  JAVA_EXE="$JAVA_HOME/bin/java"
else
  JAVA_EXE=java
fi

if ! command -v "$JAVA_EXE" >/dev/null 2>&1; then
  echo "ERROR: Java was not found. Set JAVA_HOME or add java to PATH." >&2
  exit 1
fi

exec "$JAVA_EXE" ${JAVA_OPTS:-} ${GRADLE_OPTS:-} \
  "-Dorg.gradle.appname=$(basename "$0")" \
  -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain "$@"

