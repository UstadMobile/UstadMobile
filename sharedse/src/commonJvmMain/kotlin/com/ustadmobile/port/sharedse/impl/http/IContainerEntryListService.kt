package com.ustadmobile.port.sharedse.impl.http

import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface IContainerEntryListService {

    @GET
    fun findByContainerWithMd5(@Url fullUrl: String,
                               @Query("containerUid") containerUid: Long): Call<List<ContainerEntryWithMd5>>

}
