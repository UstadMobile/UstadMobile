package com.ustadmobile.lib.annotationprocessor.core.db2

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(version = 1, entities = [ExampleEntity2::class])
abstract class ExampleDatabase2 : RoomDatabase(){

    abstract fun exampleDao2(): ExampleDao2

}