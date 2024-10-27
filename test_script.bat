@echo off
set num_clients=10
set timeout_count=0

REM Establecer el classpath al directorio de salida de Gradle
set CLASSPATH=C:\Users\firer\OneDrive\Escritorio\HelloWorldChallenge\client\build\classes\java\main

for /L %%i in (1,1,%num_clients%) do (
    start /B java -cp %CLASSPATH% Client "fibonacci 100000"
)

echo Timeouts: %timeout_count%
pause