
if not exist app-ktor-server\build\libs\ustad-server-all.jar (
    echo "Please build the server jar: gradlew app-ktor-server:shadowJar"
    exit /B 1
)

if not exist app-ktor-server\application.conf (
    copy app-ktor-server\src\jvmMain\resources\application.conf app-ktor-server
)

cd
java -jar app-ktor-server\build\libs\ustad-server-all.jar -config=app-ktor-server\application_psql.conf
