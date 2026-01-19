#!/bin/bash

# Set Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo "Using Java 17: $JAVA_HOME"
echo ""

# Verify Java version
java -version
echo ""

# Run tests
mvn clean test

# Generate coverage report
if [ $? -eq 0 ]; then
    echo ""
    echo "Tests passed! Generating coverage report..."
    mvn jacoco:report
    echo ""
    echo "Coverage report generated at: target/site/jacoco/index.html"
    echo "Open with: open target/site/jacoco/index.html"
fi