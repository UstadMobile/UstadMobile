#/bin/bash
#
# Convenience script that will build and then run the test server using normal java
# This avoids the overhead of Gradle memory usage whilst the server is running
#
cd ..
./gradlew lib-http-testserver:shadowJar
cd lib-http-testserver
java -jar build/libs/lib-http-testserver-all.jar -port=8900

