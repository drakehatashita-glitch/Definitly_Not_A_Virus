#!/bin/bash
# Run this inside Chromebook's Linux terminal (Crostini)
# It installs Java, builds the mod, and puts the JAR on your Desktop

set -e

echo ""
echo "======================================================"
echo "  Definitly not a virus - Chromebook Builder"
echo "======================================================"
echo ""

# 1. Install Java 21
echo "[1/4] Installing Java 21..."
sudo apt-get update -qq
sudo apt-get install -y openjdk-21-jdk curl unzip 2>&1 | grep -E "^(Get|Unpacking|Setting up|Processing)" || true
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
echo "      Java ready: $(java -version 2>&1 | head -1)"

# 2. Generate Gradle wrapper if missing
echo ""
echo "[2/4] Setting up Gradle..."
if [ ! -f "gradlew" ]; then
    curl -s -L "https://services.gradle.org/distributions/gradle-8.8-bin.zip" -o /tmp/gradle.zip
    unzip -q /tmp/gradle.zip -d /tmp/gradle-dist
    /tmp/gradle-dist/gradle-8.8/bin/gradle wrapper --gradle-version 8.8 --no-daemon 2>/dev/null || true
    chmod +x gradlew
fi
echo "      Gradle wrapper ready"

# 3. Build the mod
echo ""
echo "[3/4] Building mod (downloading Forge + Minecraft - takes 5-15 min first time)..."
echo "      Please wait, this is normal..."
echo ""
./gradlew build --no-daemon 2>&1

# 4. Copy jar to Desktop
echo ""
echo "[4/4] Done! Copying JAR..."

JAR_SRC=$(find build/libs -name "Definitly not a virus-*.jar" 2>/dev/null | head -1)

if [ -z "$JAR_SRC" ]; then
    echo "ERROR: Could not find built JAR. Check build output above."
    exit 1
fi

JAR_DEST="$HOME/Desktop/Definitly not a virus.jar"

# Chromebook Linux Desktop is usually here
CHROME_DESKTOP="$HOME/Desktop"
mkdir -p "$CHROME_DESKTOP"
cp "$JAR_SRC" "$JAR_DEST"

echo ""
echo "======================================================"
echo "  SUCCESS!"
echo "======================================================"
echo ""
echo "  Your mod JAR is at:"
echo "  $JAR_DEST"
echo ""
echo "  In the ChromeOS Files app, go to:"
echo "  Linux files > Desktop > Definitly not a virus.jar"
echo ""
echo "  Share that file with your friend."
echo "  They need Forge 52.1.14 for Minecraft 1.21.1."
echo "======================================================"
echo ""
