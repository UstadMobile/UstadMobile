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
echo " "
echo "You can use this to run/connect the Android client as per README.md ."
echo "If you want to use the web client in a browser, you must run "
echo "gradlew app-react:run and then open http://localhost:8087/ in your browser."
echo "See README.md for more details."
java -jar build\libs\ustad-server-all.jar %CONFIGARG% -P:ktor.ustad.jsDevServer=http://localhost:8080/
cd..
