package com.ustadmobile.stats.rest

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ustadmobile.lib.db.entities.UstadCentralReportRow
import com.ustadmobile.stats.db.StatsDatabase
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*


fun Route.StatsRoute(db: StatsDatabase, gson: Gson){

    post("receive-stats"){
        val listStr = call.receive<String>()
        val list : List<UstadCentralReportRow> = gson.fromJson(
            listStr, object: TypeToken<List<UstadCentralReportRow>>() {}.type)
        list.forEach {
            it.rowUid = 0
        }

        db.ustadCentralReportRowDao.insertList(list)

        call.respond(HttpStatusCode.NoContent, "")
    }

}