# lib-database-android


This contains the DAOs and Databases that are generated from lib-database, so that they can be
processed using Room's annotation processor.

Note: incremental annotation processing only works with JDK 8 if using the JDK bundled with Android
studio, see
[RoomProcessor.kt source](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-master-dev/room/compiler/src/main/kotlin/androidx/room/RoomProcessor.kt)
