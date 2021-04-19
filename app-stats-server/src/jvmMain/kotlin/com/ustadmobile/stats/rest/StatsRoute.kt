package com.ustadmobile.stats.rest

import com.ustadmobile.lib.db.entities.UstadCentralReportRow
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*


fun Route.StatsRoute(){

    route("stats"){

        post{

            val list = call.receive<List<UstadCentralReportRow>>()

            // get db

            // insert list to db

        }

    }

}