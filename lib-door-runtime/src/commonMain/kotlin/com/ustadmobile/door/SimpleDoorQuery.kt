package com.ustadmobile.door

expect class SimpleDoorQuery: DoorQuery {

    constructor(sql: String, values: Array<Any>?)

    constructor(sql: String)

}