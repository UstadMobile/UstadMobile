package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.on

fun Route.DevModeRoute() {

    get("UmAppDatabase/clearAllTables") {
        val db: UmAppDatabase by closestDI().on(call).instance(tag = DoorTag.TAG_DB)
        db.clearAllTables()
        call.respond("OK - cleared")
    }

}
