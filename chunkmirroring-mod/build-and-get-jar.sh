#!/bin/bash
echo ""
echo "============================================"
echo " Building: Definitly not a virus mod"
echo "============================================"
echo ""

if ! command -v java &> /dev/null; then
    echo "ERROR: Java not found!"
    echo ""
    echo "Please install Java 21 from:"
    echo "  https://adoptium.net/temurin/releases/?version=21"
    echo ""
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "Java found: $JAVA_VER"
echo ""

if [ ! -f "gradlew" ]; then
    echo "Gradle wrapper not found. Generating..."
    if ! command -v gradle &> /dev/null; then
        echo "ERROR: Neither gradlew nor gradle found."
        echo ""
        echo "Please install Gradle from: https://gradle.org/install/"
        echo "Then run: gradle wrapper --gradle-version 8.8"
        exit 1
    fi
    gradle wrapper --gradle-version 8.8
    chmod +x gradlew
fi

chmod +x gradlew

echo "Running build (this will take several minutes on first run)..."
echo "Forge is downloading Minecraft files - please wait."
echo ""

./gradlew build

if [ $? -ne 0 ]; then
    echo ""
    echo "BUILD FAILED. Check the output above for errors."
    exit 1
fi

echo ""
echo "============================================"
echo " BUILD SUCCESSFUL!"
echo "============================================"
echo ""

JAR=$(find build/libs -name "Definitly not a virus-*.jar" | head -1)
if [ -n "$JAR" ]; then
    cp "$JAR" "Definitly not a virus.jar"
    echo "Your mod jar is ready:"
    echo "  $(pwd)/Definitly not a virus.jar"
    echo ""
    echo "Drop it into your .minecraft/mods/ folder."
else
    echo "Jar built in: build/libs/"
    ls build/libs/
fi
echo ""
