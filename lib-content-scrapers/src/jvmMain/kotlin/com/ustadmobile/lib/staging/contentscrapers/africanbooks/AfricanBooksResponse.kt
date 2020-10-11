package com.ustadmobile.lib.contentscrapers.africanbooks

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AfricanBooksResponse {

    @Expose
    @SerializedName("other")
    var other: String? = null
    @Expose
    @SerializedName("approved")
    var approved: String? = null
    @Expose
    @SerializedName("app")
    var app: Boolean = false
    @Expose
    @SerializedName("dual")
    var dual: Boolean = false
    @Expose
    @SerializedName("level")
    var level: String? = null
    @Expose
    @SerializedName("lang")
    var lang: String? = null
    @Expose
    @SerializedName("people")
    var people: String? = null
    @Expose
    @SerializedName("author")
    var author: String? = null
    @Expose
    @SerializedName("summary")
    var summary: String? = null
    @Expose
    @SerializedName("date")
    var date: String? = null
    @Expose
    @SerializedName("title")
    var title: String? = null
    @Expose
    @SerializedName("id")
    var id: String? = null
}
