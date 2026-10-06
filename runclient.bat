@echo off
set JAVA_HOME=C:\Users\phumitch-window\.jdks\jdk-17.0.20+8
set PATH=%JAVA_HOME%\bin;%PATH%
call gradlew.bat runClient > runclient.log 2>&1
