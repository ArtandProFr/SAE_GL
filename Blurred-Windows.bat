@echo off
REM ============================================================
REM   BLURRED - Lancement du jeu (Windows)
REM   Double-cliquez sur ce fichier pour jouer.
REM ============================================================
cd /d "%~dp0"
java -jar Blurred.jar
if %errorlevel% neq 0 (
  echo.
  echo ------------------------------------------------------------
  echo  Le jeu n'a pas pu demarrer.
  echo  Il faut JAVA 17 ou plus recent installe sur la machine.
  echo  Telechargement gratuit : https://adoptium.net
  echo ------------------------------------------------------------
  echo.
  pause
)
