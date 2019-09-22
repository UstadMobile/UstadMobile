When converting Java to Kotlin, or writing new Kotlin, please:

* Use const val for any public static final flags. This is not the default behavior of the automatic converter. This will ensure that existing Java code can still use ClassName.CONSTANT instead of ClassName.Companion.GET_CONSTANT()

* Use @JvmStatic for any methods that are accessed as static methods from Java.

* Collections type should use List / Map, not specific implementations. Use mapOf, mutableMapOf, listOf, and mutableListOf instead of explicit type constructors (e.g. ArrayList) in Kotlin. This will ensure the Kotlin will work with Kotlin multiplatform, rather than being tied only to the JVM implementation.

* Avoid using Nullable type variables as far as possible. Only use the surety operator (!!) when you are absolutely sure a value will not be null (this results in a null pointer exception if you're wrong). This might be appropriate for a variable initialized in onCreate, and then being handled in onDestroy.

