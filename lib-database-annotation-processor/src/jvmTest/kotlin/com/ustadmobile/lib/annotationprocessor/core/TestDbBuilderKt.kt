package com.ustadmobile.lib.annotationprocessor.core

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorDatabase
import db2.ExampleDatabase2
import db2.ExampleEntity2
import org.junit.Assert
import org.junit.BeforeClass

import org.junit.Test
import java.io.File
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader

class TestDbBuilderKt {

    //@Test
    fun givenDbShouldOpen() {
        /*
        To make this run in Android Studio:
          1. Copy the jndi config (resources) to ./build/classes/test/lib-database-annotation-processor_jvmTest/
          2. Run the Gradle compile task compileTestKotlinJvm
          3. Use reflection to load classes that were created by the annotation processor
        val addMethod = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
        addMethod.isAccessible = true
        addMethod.invoke(ClassLoader.getSystemClassLoader(),
                File("/home/mike/src/UstadMobile/lib-database-annotation-processor/build/classes/kotlin/jvm/test").toURI().toURL())
        */

        var exampleDb2 = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class, "db1").build()
        Assert.assertNotNull(exampleDb2)
        val exampleDao2 = exampleDb2.exampleDao2()
        Assert.assertNotNull(exampleDao2)
        val exList = listOf(ExampleEntity2(0, "bob",42))
        exampleDao2.insertList(exList)
        Assert.assertTrue(true)
    }


    @Test
    fun givenEntryInserted_whenQueried_shouldBeEqual() {
        val exampleDb = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class, "db1").build()
        val entityToInsert = ExampleEntity2(0, "Bob", 50)
        entityToInsert.uid = exampleDb.exampleDao2().insertAndReturnId(entityToInsert)

        val entityFromQuery = exampleDb.exampleDao2().findByUid(entityToInsert.uid)

        Assert.assertNotEquals(0, entityToInsert.uid)
        Assert.assertEquals("Entity retrieved from database is the same as entity inserted",
                entityToInsert, entityFromQuery)
    }

}