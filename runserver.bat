@echo off

gradlew app-ktor-server:shadowJar

if not exist app-ktor-server\build\libs\ustad-server-all.jar (
    echo "Please build the server jar: gradlew app-ktor-server:buildFatJar"
    exit /B 1
)

SET "CONFIGARG= "

if exist app-ktor-server\ustad-server.conf (
    SET "CONFIGARG=-config=ustad-server.conf"
)

cd app-ktor-server

REM Start the server - use jsDevServer to serve client using webpack (see doc on application.conf)
echo "Starting Ustad HTTP/REST server on port 8087 - Use [Ctrl+C] to stop."
java -jar build\libs\ustad-server-all.jar %CONFIGARG% -P:ktor.ustad.jsDevServer=http://localhost:8080/
cd..
