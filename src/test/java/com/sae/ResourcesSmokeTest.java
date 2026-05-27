package com.sae;

import com.sae.core.FileManager;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Petit programme de validation manuelle (sans framework de test pour rester
 * léger) : vérifie que toutes les ressources critiques sont accessibles via
 * le classpath et que le dossier de données utilisateur est inscriptible.
 *
 * Exécuter avec :
 *   mvn -q compile exec:java -Dexec.mainClass=com.sae.ResourcesSmokeTest \
 *       -Dexec.classpathScope=compile
 *
 * (ou via la commande équivalente java -cp ... exécutée par notre script de test)
 */
public class ResourcesSmokeTest {

    private static int failures = 0;

    public static void main(String[] args) {
        String[] classpathResources = {
                "/assets/background.png",
                "/style.css",
                "/fonts/Minecraft.ttf",
                "/images/Thomas/image_accueil.jpg",
                "/images/Thomas/image_menu.jpg",
                "/images/Thomas/pierre1.jpg",
                "/images/Thomas/louis1.jpg",
        };

        for (String r : classpathResources) {
            checkClasspath(r);
        }

        // Vérifie le dossier d'écriture pour les sauvegardes.
        Path savesDir = FileManager.userDir.resolve("Saves");
        if (Files.isDirectory(savesDir) && Files.isWritable(savesDir)) {
            System.out.println("OK   Dossier sauvegardes inscriptible : " + savesDir);
        } else {
            System.out.println("FAIL Dossier sauvegardes manquant ou non inscriptible : " + savesDir);
            failures++;
        }

        if (failures > 0) {
            System.err.println(failures + " ressource(s) introuvable(s).");
            System.exit(1);
        }
        System.out.println("Toutes les ressources sont chargees correctement.");
    }

    private static void checkClasspath(String path) {
        if (ResourcesSmokeTest.class.getResource(path) != null) {
            System.out.println("OK   " + path);
        } else {
            System.out.println("FAIL " + path);
            failures++;
        }
    }
}
