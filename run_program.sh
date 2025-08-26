#!/bin/bash

# Navigate to the project directory
cd /Users/dor.kokotek/Git/JavaS-Emulator

# Compile the project (skip tests for faster startup)
mvn clean install -DskipTests

# Run the main class
java -jar s-emulator-ui/target/s-emulator-ui-1.0.0-jar-with-dependencies.jar
