@echo off
title Schematic Game
cd /d "%~dp0"
echo Iniciando Schematic Game...
call .\gradlew.bat lwjgl3:run
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Ocurrio un error al ejecutar el juego.
    pause
)

