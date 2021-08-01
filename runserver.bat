
if not exist app-ktor-server\build\libs\ustad-server-all.jar (
    echo "Please build the server jar: gradlew app-ktor-server:shadowJar"
    exit /B 1
)

if not exist app-ktor-server\application.conf (
    copy app-ktor-server\src\jvmMain\resources\application.conf app-ktor-server
)

cd app-ktor-server
java -jar build\libs\ustad-server-all.jar -config=application.conf
cd..
