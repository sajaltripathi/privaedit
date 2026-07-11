@echo off
title PrivaEdit - Offline APK Generator
color 0B

:: Force current directory to be the directory containing this batch script
cd /d "%~dp0"

echo ===================================================
echo           PrivaEdit Offline APK Generator           
echo ===================================================
echo.

:: Step 1: Check if gradlew.bat exists in the current folder
if not exist "gradlew.bat" (
    echo ---------------------------------------------------
    echo ERROR: gradlew.bat was not found in this folder!
    echo Current folder is: %CD%
    echo ---------------------------------------------------
    echo Please make sure this "build_apk.bat" script is inside
    echo your extracted project folder where "gradlew.bat" is located.
    echo ---------------------------------------------------
    echo.
    pause
    exit /b
)

:: Step 2: Check if Java is installed
echo [1/4] Checking environment structure...
echo [OK] Project environment looks correct!
echo.
echo [2/4] Checking for Java Development Kit (JDK)...
java -version >nul 2>&1
if errorlevel 1 goto NO_JAVA

echo [OK] Java detected successfully!
echo.

:: Step 3: Compile the APK
echo [3/4] Compiling source code to APK...
echo This will take a moment. Please wait...
echo.

call gradlew.bat assembleDebug
if errorlevel 1 goto BUILD_FAILED

echo.
echo [OK] Compilation finished successfully!
echo.

:: Step 4: Copying and organizing the output APK
echo [4/4] Copying the compiled APK...
if not exist "app\build\outputs\apk\debug\app-debug.apk" goto NO_APK

copy /y "app\build\outputs\apk\debug\app-debug.apk" "PrivaEdit-debug.apk" >nul
echo.
echo ===================================================
echo SUCCESS! Your APK has been compiled successfully.
echo File Location: "%~dp0PrivaEdit-debug.apk"
echo ===================================================
echo.
explorer.exe /select,"%~dp0PrivaEdit-debug.apk"
goto END

:NO_JAVA
echo.
echo ---------------------------------------------------
echo ERROR: Java is not installed or not in your PATH!
echo ---------------------------------------------------
echo To compile an Android APK, a lightweight Java Compiler (JDK) is required.
echo YOU DO NOT NEED TO INSTALL ANDROID STUDIO.
echo.
echo How to install Java 17 instantly:
echo 1. Open a NEW Command Prompt (cmd.exe) as Administrator.
echo 2. Run this simple command:
echo    winget install EclipseAdoptium.Temurin.17.JDK
echo.
echo 3. Close this window, open a new Command Prompt, and run "build_apk.bat" again.
echo ---------------------------------------------------
echo.
pause
exit /b

:BUILD_FAILED
echo.
echo ---------------------------------------------------
echo ERROR: Gradle build failed.
echo Please make sure your project is intact and unmodified.
echo ---------------------------------------------------
echo.
pause
exit /b

:NO_APK
echo.
echo ERROR: APK was compiled but could not be located in the build folder.
echo Please check "app\build\outputs\apk\debug\" manually.
echo.
pause
exit /b

:END
pause
