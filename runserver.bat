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
java -jar build\libs\ustad-server-all.jar %SITEURLARG% %CONFIGARG% %1 %2 %3 %4
cd..
