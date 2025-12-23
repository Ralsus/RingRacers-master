@rem Gradle Wrapper Launcher for Windows
@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem  Gradle startup script for Windows
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

set WRAPPER_JAR="%APP_HOME%\gradle\wrapper\gradle-wrapper.jar"

@rem Download gradle-wrapper.jar if it doesn't exist
if not exist %WRAPPER_JAR% (
    echo Downloading Gradle Wrapper...
    mkdir "%APP_HOME%\gradle\wrapper" 2>nul
    powershell -Command "(New-Object Net.WebClient).DownloadFile('https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar', '%APP_HOME%\gradle\wrapper\gradle-wrapper.jar')"
)

set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
goto fail

:execute
@rem Setup the command line

set CLASSPATH=%WRAPPER_JAR%

@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% -classpath %CLASSPATH% org.gradle.wrapper.GradleWrapperMain %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal
