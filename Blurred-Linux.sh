#!/bin/bash
# ============================================================
#   BLURRED - Lancement du jeu (Linux)
#   Lancez ce fichier (double-clic > Exécuter, ou ./Blurred-Linux.sh)
# ============================================================
cd "$(dirname "$0")"
if ! command -v java >/dev/null 2>&1; then
  echo "Java 17+ est requis. Installez-le (ex : sudo apt install openjdk-17-jre) ou https://adoptium.net"
  read -n 1 -s -r -p "Appuyez sur une touche pour fermer..."
  exit 1
fi
java -jar Blurred.jar
