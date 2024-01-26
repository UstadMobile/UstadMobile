#-keep public class com.ustadmobile.port.desktop.AppKt {
#    public static void main(java.lang.String[]);
#}
#-dontnote *

-dontnote *

#OKHttp
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keeppackagenames okhttp3.internal.publicsuffix.*
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**


#Quartz
# We are not using Servlet
-dontwarn javax.servlet.**

#Quartz does not seem like to like being obfuscated
-keep class org.quartz.** { *; }
-keep class org.quartz.impl.jdbcjobstore.JobStoreTX { * ; }
-keep class com.ustadmobile.core.schedule.SqliteJDBCDelegate { * ; }

#not using terracotta...
-dontwarn org.terracotta.toolkit.**
-dontwarn weblogic.jdbc.jts.**
-dontwarn javax.transaction.**
-dontwarn org.quartz.ee.servlet.QuartzInitializerServlet
-dontwarn oracle.sql.**
-dontwarn weblogic.jdbc.**
-dontwarn javax.xml.bind.**
-dontwarn commonj.work.Work
-dontwarn org.jboss.system.ServiceMBeanSupport
-dontwarn org.quartz.jobs.FileScanListener
-dontwarn org.jboss.system.ServiceMBean
-dontwarn org.jboss.logging.**
-dontwarn commonj.work.**
-dontwarn org.jboss.naming.**
-dontwarn org.quartz.jobs.FileScanJob

#Hikari (not used directly, dependency comes in via Quartz)
#We are not using Hibernate
-dontwarn org.hibernate.**
-dontwarn com.codahale.metrics.**
-dontwarn io.prometheus.client.**
-dontwarn io.micrometer.core.instrument.**
-dontwarn com.zaxxer.hikari.metrics.prometheus.**
-dontwarn javassist.**

#Byte Buddy
-dontwarn com.sun.jna.Library
-dontwarn com.sun.jna.FunctionMapper
-dontwarn org.apache.commons.pool.**
-dontwarn com.sun.jna.**

#Jogamp - we are not using SWT
-dontwarn org.eclipse.swt.**

#not using findbugs
-dontwarn edu.umd.cs.findbugs.annotations.**

#Batik (SVG loader)
# we are not using Javascript in SVG
-dontwarn org.mozilla.javascript.**
-dontwarn org.python.util.**
-dontwarn org.python.core.**

# Google 'stuff'
-dontwarn com.google.j2objc.annotations.**

#Not using log4j
-dontwarn org.apache.log4j.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.apache.log.**

#Door - not using Postgres on Desktop
-dontwarn org.postgresql.jdbc.**
-dontwarn org.postgresql.**

#Not using apache logger
-dontwarn org.apache.avalon.framework.logger.**


# Library depending on program class: org.w3c.dom - for some reason jlink java.xml is considered program class?
# Results in "library class depends on program class" proguard errors.
-keep class org.w3c.dom.** { *; }
-dontwarn org.w3c.dom.**

-keep class org.xml.sax.** { *; }
-dontwarn org.xml.sax.**

-keep class javax.xml.** { *; }
-dontwarn javax.xml.**


# "unresolved" references to program class member
-dontwarn java.util.zip.Checksum
-dontwarn com.google.common.hash.Hashing$Crc32cMethodHandles

-dontwarn com.zaxxer.hikari.metrics.PoolStats
-dontwarn com.zaxxer.hikari.pool.HikariPool$KeepaliveTask
-dontwarn com.zaxxer.hikari.pool.HikariPool$MaxLifetimeTask
-dontwarn com.zaxxer.hikari.pool.ProxyLeakTaskFactory

-dontwarn nl.adaptivity.xmlutil.StAXWriter

-dontwarn org.apache.batik.bridge.BatikWrapFactory
-dontwarn org.apache.batik.bridge.GlobalWrapper
-dontwarn org.apache.batik.bridge.WindowWrapper
-dontwarn org.apache.batik.bridge.EventTargetWrapper
-dontwarn org.apache.pdfbox.io.IOUtils

-dontwarn org.quartz.ee.jmx.jboss.QuartzService


# As per KodeIn DI docs:
# https://kosi-libs.org/kodein/7.19/framework/android.html#_proguard_configuration
-dontwarn java.lang.invoke.StringConcatFactory

-keep, allowobfuscation, allowoptimization class org.kodein.type.TypeReference
-keep, allowobfuscation, allowoptimization class org.kodein.type.JVMAbstractTypeToken$Companion$WrappingTest

-keep, allowobfuscation, allowoptimization class * extends org.kodein.type.TypeReference
-keep, allowobfuscation, allowoptimization class * extends org.kodein.type.JVMAbstractTypeToken$Companion$WrappingTest

# Required for TypeReference to keep type parameters
-keepattributes Signature

#Simple JNDI does not cooperate with obfuscation. It will also be looked up by name as per the
# jndi.properties, not worth obfuscating
-keep class org.osjava.sj.** { *; }

#SQLiteJDBC - Uses JAVA SPI
-keep class org.sqlite.** { *; }

-keep public class com.ustadmobile.core.db.**{
    public <init>(...);
}

# Old apache commons appears to be referenced by SimpleJNDI
-keep class org.apache.commons.pool.**
-keep class org.apache.commons.dbcp.**

### Begin KTOR https://github.com/ktorio/ktor/blob/main/ktor-utils/jvm/resources/META-INF/proguard/ktor.pro

# Most of volatile fields are updated with AtomicFU and should not be mangled/removed
-keepclassmembers class io.ktor.** {
    volatile <fields>;
}

-keepclassmembernames class io.ktor.** {
    volatile <fields>;
}

# client engines are loaded using ServiceLoader so we need to keep them
-keep class io.ktor.client.engine.** implements io.ktor.client.HttpClientEngineContainer


### END KTOR https://github.com/ktorio/ktor/blob/main/ktor-utils/jvm/resources/META-INF/proguard/ktor.pro

#-keep class io.ktor.** { * ; }

# KTOR Client users service provision to load - so these classes must not be obfuscated
-keep class io.ktor.client.engine.okhttp.OkHttp { * ; }
-keep class io.ktor.client.engine.okhttp.OkHttpEngineContainer { * ; }
-keep interface io.ktor.client.HttpClientEngineContainer { * ; }


# Keep database impl classes, repositories, etc that are looked up by reflection
-keep class * extends com.ustadmobile.door.room.RoomDatabase { public <init>(...); }

### As per Door proguard-rules.pro

-keep public class * extends com.ustadmobile.door.ext.DoorDatabaseMetadata {
    *;
}

# On databases that don't use replication, the DoorDatabaseWrapper constructor was removed
-keepclassmembers public class * extends com.ustadmobile.door.DoorDatabaseWrapper {
    public <init>(...);
}

### End door proguard rules


#Compose - not documented

# Service provider interface from Swing Coroutines https://github.com/Kotlin/kotlinx.coroutines/blob/master/ui/kotlinx-coroutines-swing/resources/META-INF/services/kotlinx.coroutines.internal.MainDispatcherFactory
-keep class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keep class kotlinx.coroutines.swing.SwingDispatcherFactory { *; }


#Kotlinx Serialization rules as per https://github.com/Kotlin/kotlinx.serialization/blob/master/rules/common.pro
# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Don't print notes about potential mistakes or omissions in the configuration for kotlinx-serialization classes
# See also https://github.com/Kotlin/kotlinx.serialization/issues/1900
-dontnote kotlinx.serialization.**

# Serialization core uses `java.lang.ClassValue` for caching inside these specified classes.
# If there is no `java.lang.ClassValue` (for example, in Android), then R8/ProGuard will print a warning.
# However, since in this case they will not be used, we can disable these warnings
-dontwarn kotlinx.serialization.internal.ClassValueReferences


### END Kotlinx serialization rules from site

### More for entities


#Keep anything with the Serializable annotation. We are using Gson to avoid kotlinx serialization
# errors, but GSON requires classes to be exempted from obfuscation
-keep @kotlinx.serialization.Serializable class * {
    *;
}

# umDatabase entities
-keep public class com.ustadmobile.lib.db.entities.**{
       *;
}
-keep public class com.ustadmobile.core.db.**{
    public <init>(...);
}


# Entities from Door

#
# Note: It seems like with Proguard on JVM, we need to keep the whole package for the Serializer
# to work.
#
-keep public class com.ustadmobile.door.message.** {
    * ;
}

-keep public class com.ustadmobile.door.entities.** {
    * ;
}

-keep public class com.ustadmobile.door.SyncNode {
    * ;
}

