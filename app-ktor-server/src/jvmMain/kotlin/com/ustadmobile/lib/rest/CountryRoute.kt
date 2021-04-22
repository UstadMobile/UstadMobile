package com.ustadmobile.lib.rest

import com.maxmind.geoip2.DatabaseReader
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.apache.commons.io.FileUtils
import org.kodein.di.instance
import org.kodein.di.ktor.di
import java.io.File
import java.net.InetAddress

fun Route.CountryRoute() {

    route("country"){
        get("code"){

            try {
                    val reader: DatabaseReader by di().instance()
                    val ipAddress = call.request.origin.remoteHost

                    val address = InetAddress.getByName(ipAddress)
                    val countryResponse = reader.country(address)
                    val code = countryResponse.country.isoCode

                    call.respond(code)
            }catch (e: Exception){
                call.respond(HttpStatusCode.InternalServerError, "Upload error: $e")
            }

        }
    }


}