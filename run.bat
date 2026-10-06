@echo off
chcp 65001 >nul
cd /d %~dp0
set JAVA_HOME=C:\Users\phumitch-window\.jdks\jdk-17.0.20+8
echo ============================================
echo  ModTitle - กำลังเปิดเกม (ใช้เวลา 1-3 นาที)
echo  อย่าปิดหน้าต่างนี้จนกว่าเกมจะขึ้น
echo ============================================
call gradlew.bat runClient
echo.
echo เกมปิดแล้ว
pause
