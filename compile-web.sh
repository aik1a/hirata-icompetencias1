#!/bin/bash
# compile-web.sh — Recompila el backend Java (ApiServer) y empaqueta el fat-jar
# Requiere: Java 21+, Maven instalado (o JARs ya descargados en ~/.m2)

set -e
cd "$(dirname "$0")"

M2="$HOME/.m2/repository"

# Classpath de compilación: JARs de Maven local + MySQL connector local
CP="\
$M2/io/javalin/javalin/6.4.0/javalin-6.4.0.jar:\
$M2/com/fasterxml/jackson/core/jackson-databind/2.17.2/jackson-databind-2.17.2.jar:\
$M2/com/fasterxml/jackson/core/jackson-core/2.17.2/jackson-core-2.17.2.jar:\
$M2/com/fasterxml/jackson/core/jackson-annotations/2.17.2/jackson-annotations-2.17.2.jar:\
$M2/org/eclipse/jetty/jetty-server/11.0.24/jetty-server-11.0.24.jar:\
$M2/org/eclipse/jetty/jetty-http/11.0.24/jetty-http-11.0.24.jar:\
$M2/org/eclipse/jetty/jetty-io/11.0.24/jetty-io-11.0.24.jar:\
$M2/org/eclipse/jetty/jetty-util/11.0.24/jetty-util-11.0.24.jar:\
$M2/org/eclipse/jetty/jetty-security/11.0.24/jetty-security-11.0.24.jar:\
$M2/org/eclipse/jetty/jetty-servlet/11.0.24/jetty-servlet-11.0.24.jar:\
$M2/org/eclipse/jetty/jetty-webapp/11.0.24/jetty-webapp-11.0.24.jar:\
$M2/org/eclipse/jetty/jetty-xml/11.0.24/jetty-xml-11.0.24.jar:\
$M2/org/eclipse/jetty/toolchain/jetty-jakarta-servlet-api/5.0.2/jetty-jakarta-servlet-api-5.0.2.jar:\
$M2/org/eclipse/jetty/websocket/websocket-core-server/11.0.24/websocket-core-server-11.0.24.jar:\
$M2/org/eclipse/jetty/websocket/websocket-core-common/11.0.24/websocket-core-common-11.0.24.jar:\
$M2/org/eclipse/jetty/websocket/websocket-jetty-api/11.0.24/websocket-jetty-api-11.0.24.jar:\
$M2/org/eclipse/jetty/websocket/websocket-jetty-common/11.0.24/websocket-jetty-common-11.0.24.jar:\
$M2/org/eclipse/jetty/websocket/websocket-jetty-server/11.0.24/websocket-jetty-server-11.0.24.jar:\
$M2/org/eclipse/jetty/websocket/websocket-servlet/11.0.24/websocket-servlet-11.0.24.jar:\
$M2/org/jetbrains/kotlin/kotlin-stdlib/1.9.25/kotlin-stdlib-1.9.25.jar:\
$M2/org/jetbrains/kotlin/kotlin-stdlib-jdk7/1.9.25/kotlin-stdlib-jdk7-1.9.25.jar:\
$M2/org/jetbrains/kotlin/kotlin-stdlib-jdk8/1.9.25/kotlin-stdlib-jdk8-1.9.25.jar:\
$M2/org/jetbrains/annotations/13.0/annotations-13.0.jar:\
$M2/org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar:\
$M2/org/slf4j/slf4j-simple/2.0.13/slf4j-simple-2.0.13.jar:\
lib/mysql-connector-j-9.6.0.jar"

echo "Compilando src/main/java/com/hirata/*.java ..."
mkdir -p target/classes
javac -cp "$CP" -d target/classes \
  src/main/java/com/hirata/ConexionDB.java \
  src/main/java/com/hirata/ApiServer.java

echo "Empaquetando fat-jar en target/hirata-flota-api.jar ..."
mkdir -p target/fatjar

# Extraer todos los JARs de dependencias
cd target/fatjar
for jar in $(echo "$CP" | tr ':' '\n'); do
  [ -f "$jar" ] && jar xf "$jar"
done
# Copiar clases compiladas
cp -r ../classes/* .
# Crear MANIFEST
mkdir -p META-INF
echo "Main-Class: com.hirata.ApiServer" > META-INF/MANIFEST.MF
# Empaquetar
jar cfm ../hirata-flota-api.jar META-INF/MANIFEST.MF .
cd ../..

echo "Listo: target/hirata-flota-api.jar ($(du -sh target/hirata-flota-api.jar | cut -f1))"
