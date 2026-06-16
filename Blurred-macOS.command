#!/bin/bash
# ============================================================
#   BLURRED - Lancement du jeu (macOS)
#   Double-cliquez sur ce fichier pour jouer.
# ============================================================
cd "$(dirname "$0")"
if ! command -v java >/dev/null 2>&1; then
  echo "Java 17+ est requis. Telechargement gratuit : https://adoptium.net"
  read -n 1 -s -r -p "Appuyez sur une touche pour fermer..."
  exit 1
fi
java -jar Blurred.jar
