@echo off
setlocal ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
for %%i in ("%DIRNAME%.") do set APP_HOME=%%~fi

set DIST_URL=https://services.gradle.org/distributions/gradle-8.10.2-bin.zip
set DIST_NAME=gradle-8.10.2-bin.zip
set DIST_DIR_NAME=gradle-8.10.2

if "%GRADLE_USER_HOME%"=="" set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set WRAPPER_DIR=%GRADLE_USER_HOME%\wrapper\dists\%DIST_DIR_NAME%
set ZIP_PATH=%WRAPPER_DIR%\%DIST_NAME%
set GRADLE_HOME=%WRAPPER_DIR%\%DIST_DIR_NAME%
set GRADLE_EXE=%GRADLE_HOME%\bin\gradle.bat

if not exist "%GRADLE_EXE%" (
  echo Downloading Gradle %DIST_DIR_NAME%...
  if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"

  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%ZIP_PATH%'"
  if errorlevel 1 goto fail

  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "Expand-Archive -Path '%ZIP_PATH%' -DestinationPath '%WRAPPER_DIR%' -Force"
  if errorlevel 1 goto fail
)

if not exist "%GRADLE_EXE%" (
  echo ERROR: Could not prepare Gradle at "%GRADLE_EXE%".
  goto fail
)

call "%GRADLE_EXE%" -p "%APP_HOME%" %*
exit /b %ERRORLEVEL%

:fail
exit /b 1
