package com.ustadmobile.lib.annotationprocessor.core

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorDatabase
import db2.ExampleDatabase2
import org.junit.Assert

import org.junit.Test

class TestDbBuilderKt {

    @Test
    fun givenDbShouldOpen() {
        var exampleDb2 = DatabaseBuilder.databaseBuilder(Any(), ExampleDatabase2::class, "db1").build()
        Assert.assertNotNull(exampleDb2)
        val exampleDao2 = exampleDb2.exampleDao2()
        Assert.assertNotNull(exampleDao2)
    }

}