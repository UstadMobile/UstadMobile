
if not exist app-ktor-server\build\libs\ustad-server-all.jar (
    echo "Please build the server jar: gradlew app-ktor-server:shadowJar"
    exit /B 1
)

if not exist app-ktor-server\ustad-server.conf (
    copy app-ktor-server\src\main\resources\application.conf app-ktor-server\ustad-server.conf
)

cd app-ktor-server
java -jar build\libs\ustad-server-all.jar -config=ustad-server.conf
cd..
