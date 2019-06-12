package com.ustadmobile.lib.contentscrapers.voa

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class VoaResponse {

    @Expose
    @SerializedName("articleSection")
    var articleSection: String? = null
    @Expose
    @SerializedName("description")
    var description: String? = null

    @Expose
    @SerializedName("keywords")
    var keywords: String? = null
    @Expose
    @SerializedName("dateModified")
    var dateModified: String? = null
    @Expose
    @SerializedName("datePublished")
    var datePublished: String? = null

    @Expose
    @SerializedName("name")
    var name: String? = null
    @Expose
    @SerializedName("headline")
    var headline: String? = null
    @Expose
    @SerializedName("url")
    var url: String? = null
    @Expose
    @SerializedName("mainEntityOfPage")
    var mainEntityOfPage: String? = null
    @Expose
    @SerializedName("inLanguage")
    var inLanguage: String? = null

}
