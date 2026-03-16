#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GRADLE_VERSION="${GRADLE_VERSION:-8.7}"
WRAPPER_DIR="$SCRIPT_DIR/.gradle-wrapper"
GRADLE_HOME="$WRAPPER_DIR/gradle-$GRADLE_VERSION"
GRADLE_BIN="$GRADLE_HOME/bin/gradle"
GRADLE_ZIP="$WRAPPER_DIR/gradle-$GRADLE_VERSION-bin.zip"
GRADLE_URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"

download_gradle() {
  mkdir -p "$WRAPPER_DIR"
  if [ ! -f "$GRADLE_ZIP" ]; then
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL "$GRADLE_URL" -o "$GRADLE_ZIP"
    elif command -v wget >/dev/null 2>&1; then
      wget -qO "$GRADLE_ZIP" "$GRADLE_URL"
    else
      echo "curl or wget is required to download Gradle." >&2
      exit 1
    fi
  fi

  if [ ! -x "$GRADLE_BIN" ]; then
    rm -rf "$GRADLE_HOME"
    unzip -q -o "$GRADLE_ZIP" -d "$WRAPPER_DIR"
  fi
}

if [ ! -x "$GRADLE_BIN" ]; then
  download_gradle
fi

exec "$GRADLE_BIN" "$@"
