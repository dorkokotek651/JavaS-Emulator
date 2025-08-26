@echo off
echo Starting S-Emulator Application...
java -jar s-emulator-ui-1.0.0-jar-with-dependencies.jar
if %ERRORLEVEL% neq 0 (
    echo Error: Failed to start S-Emulator Application
    echo Make sure Java is installed and accessible from PATH
    pause
    exit /b %ERRORLEVEL%
)
