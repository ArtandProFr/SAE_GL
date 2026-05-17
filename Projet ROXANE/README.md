# Escape Game JavaFX

Ce projet contient un menu principal d'escape game en JavaFX + CSS, avec une classe `Bouton` personnalisee.

## Prerequis installes

- JDK Temurin 21
- Apache Maven 3.9.6 (installe dans `C:\Users\HP\tools\apache-maven-3.9.6`)

## Lancer l'application

Dans un terminal PowerShell ouvert dans ce dossier:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
$env:M2_HOME = "$env:USERPROFILE\tools\apache-maven-3.9.6"
$env:Path = "$env:M2_HOME\\bin;$env:JAVA_HOME\\bin;" + $env:Path
mvn javafx:run
```

## Structure

- `src/main/java/com/roxane/app/MainApp.java`
- `src/main/java/com/roxane/app/MenuPrincipal.java`
- `src/main/java/com/roxane/app/Bouton.java`
- `src/main/resources/styles/escape-game.css`
- `pom.xml`
