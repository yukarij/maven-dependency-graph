@echo off
setlocal
cd /d "%~dp0"
if not defined JAVA_HOME goto check_path
set "PATH=%JAVA_HOME%\bin;%PATH%"
:check_path
if not exist build mkdir build
dir /s /b src\*.java tests\*.java > build\tests.txt
javac --release 17 -encoding UTF-8 -d build @build\tests.txt
if errorlevel 1 exit /b 1
java -ea -cp build depgraph.AllTests
exit /b %errorlevel%
