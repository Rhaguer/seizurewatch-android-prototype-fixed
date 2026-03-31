#!/usr/bin/env sh
set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
DIST_URL="https://services.gradle.org/distributions/gradle-8.10.2-bin.zip"
DIST_NAME="gradle-8.10.2-bin.zip"
DIST_DIR_NAME="gradle-8.10.2"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
WRAPPER_DIR="$GRADLE_USER_HOME/wrapper/dists/$DIST_DIR_NAME"
ZIP_PATH="$WRAPPER_DIR/$DIST_NAME"
GRADLE_HOME="$WRAPPER_DIR/$DIST_DIR_NAME"
GRADLE_EXE="$GRADLE_HOME/bin/gradle"

if [ ! -x "$GRADLE_EXE" ]; then
  echo "Downloading Gradle $DIST_DIR_NAME..."
  mkdir -p "$WRAPPER_DIR"
  if command -v curl >/dev/null 2>&1; then
    curl -L "$DIST_URL" -o "$ZIP_PATH"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP_PATH" "$DIST_URL"
  else
    echo "ERROR: curl or wget is required to download Gradle." >&2
    exit 1
  fi
  unzip -o "$ZIP_PATH" -d "$WRAPPER_DIR" >/dev/null
fi

exec "$GRADLE_EXE" -p "$APP_HOME" "$@"
