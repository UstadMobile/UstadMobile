package db2

import android.arch.persistence.room.Embedded

class ExampleEntity2WithExampleLinkEntity(uid: Long, name: String, someNumber: Long): ExampleEntity2(uid, name, someNumber){

    @Embedded
    var link: ExampleLinkEntity? = null

    constructor(): this(0, "", 0) {

    }



}