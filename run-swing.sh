#!/bin/bash
# run-swing.sh — Compila y lanza la interfaz Swing legacy (MenuPrincipal)
# Requiere: Java 21+, JARs en lib/

set -e
cd "$(dirname "$0")"

mkdir -p bin

echo "Compilando interfaz Swing..."
javac -cp "lib/*" -d bin \
  src/ConexionDB.java \
  src/FormularioCamiones.java \
  src/FormularioConductores.java \
  src/FormularioKilometraje.java \
  src/FormularioKilometrajeFlatLaf.java \
  src/FormularioMantenimiento.java \
  src/MenuPrincipal.java

echo "Lanzando MenuPrincipal..."
java -cp "bin:lib/*" MenuPrincipal
