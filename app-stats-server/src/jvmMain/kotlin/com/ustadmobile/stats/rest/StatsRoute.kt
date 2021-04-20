package com.ustadmobile.stats.rest

import com.ustadmobile.lib.db.entities.UstadCentralReportRow
import com.ustadmobile.stats.db.StatsDatabase
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*


fun Route.StatsRoute(db: StatsDatabase){

    route("stats"){

        post("/"){

            val list = call.receive<List<UstadCentralReportRow>>()
            db.ustadCentralReportRowDao.insertList(list)

        }

    }

}