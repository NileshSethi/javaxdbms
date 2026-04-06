::@echo off
::echo.
::echo  ==========================================
::echo   GamerSync - Compile and Run
::echo  ==========================================
::echo.
::
::cd src
::
::echo  [1/2] Compiling...
::javac -cp ".;..\lib\*" gamersync\db\*.java gamersync\model\*.java gamersync\dao\*.java gamersync\service\*.java gamersync\Main.java
::
::IF %ERRORLEVEL% NEQ 0 (
::    echo.
::    echo  [ERROR] Compilation failed. Check errors above.
::    pause
::    exit /b 1
::)
::
::echo  [2/2] Compilation successful. Starting GamerSync...
::echo.
::java -cp ".;..\lib\*" gamersync.Main
::
::pause

@echo off
echo   GamerSync - Compile and Run
echo  ==========================================

echo  [1/2] Compiling...
javac -cp ".;.\lib\*" gamersync\db\*.java gamersync\model\*.java gamersync\dao\*.java gamersync\service\*.java gamersync\Main.java

IF %ERRORLEVEL% NEQ 0 (
    echo  [ERROR] Compilation failed. Check errors above.
    pause
    exit /b 1
)

echo  [2/2] Compilation successful. Starting GamerSync...
java -cp ".;.\lib\*" gamersync.Main
pause
