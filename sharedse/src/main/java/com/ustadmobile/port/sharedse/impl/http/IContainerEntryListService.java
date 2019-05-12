package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface IContainerEntryListService {

    @GET
    Call<List<ContainerEntryWithMd5>> findByContainerWithMd5(@Url String fullUrl,
                                                             @Query("containerUid") long containerUid);

}
