#!/bin/bash
# run-web.sh — Lanza el dashboard web moderno
# Requiere: Java 21+, target/hirata-flota-api.jar compilado
#
# Modo producción (solo backend, sirve frontend/dist/):
#   ./run-web.sh
#   Abrir: http://localhost:7000
#
# Modo desarrollo (con hot-reload del frontend):
#   Terminal 1: ./run-web.sh
#   Terminal 2: cd frontend && npm run dev
#   Abrir: http://localhost:5173

set -e
cd "$(dirname "$0")"

JAR="target/hirata-flota-api.jar"

if [ ! -f "$JAR" ]; then
  echo "ERROR: No se encontró $JAR"
  echo "Ejecuta primero: ./compile-web.sh"
  exit 1
fi

echo "Iniciando API REST en http://localhost:7000 ..."
echo "Dashboard disponible en http://localhost:7000"
echo "(Para desarrollo con hot-reload: cd frontend && npm run dev → http://localhost:5173)"
echo ""
java -jar "$JAR"
