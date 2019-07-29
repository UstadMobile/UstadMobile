package com.ustadmobile.lib.contentscrapers.prathambooks

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BooksResponse {

    @Expose
    @SerializedName("data")
    var data: List<Data>? = null
    @Expose
    @SerializedName("metadata")
    var metadata: Metadata? = null

    @Expose
    @SerializedName("ok")
    var ok: Boolean = false

    class Data {
        @Expose
        @SerializedName("isGif")
        var isGif: Boolean = false
        @Expose
        @SerializedName("audioStatus")
        var audioStatus: String? = null
        @Expose
        @SerializedName("isAudio")
        var isAudio: Boolean = false
        @Expose
        @SerializedName("availableForOfflineMode")
        var availableForOfflineMode: Boolean = false
        @Expose
        @SerializedName("publisher")
        var publisher: Publisher? = null
        @Expose
        @SerializedName("description")
        var description: String? = null
        @Expose
        @SerializedName("likesCount")
        var likesCount: Int = 0
        @Expose
        @SerializedName("readsCount")
        var readsCount: Int = 0
        @Expose
        @SerializedName("illustrators")
        var illustrators: List<Illustrators>? = null
        @Expose
        @SerializedName("storyDownloaded")
        var storyDownloaded: Boolean = false
        @Expose
        @SerializedName("authors")
        var authors: List<Authors>? = null
        @Expose
        @SerializedName("coverImage")
        var coverImage: CoverImage? = null
        @Expose
        @SerializedName("editorsPick")
        var editorsPick: Boolean = false
        @Expose
        @SerializedName("recommended")
        var recommended: Boolean = false
        @Expose
        @SerializedName("slug")
        var slug: String? = null
        @Expose
        @SerializedName("level")
        var level: String? = null
        @Expose
        @SerializedName("language")
        var language: String? = null
        @Expose
        @SerializedName("title")
        var title: String? = null
        @Expose
        @SerializedName("id")
        var id: Int = 0
    }

    class Publisher {
        @Expose
        @SerializedName("logo")
        var logo: String? = null
        @Expose
        @SerializedName("slug")
        var slug: String? = null
        @Expose
        @SerializedName("name")
        var name: String? = null
    }

    class Illustrators {
        @Expose
        @SerializedName("slug")
        var slug: String? = null
        @Expose
        @SerializedName("name")
        var name: String? = null
    }

    class Authors {
        @Expose
        @SerializedName("name")
        var name: String? = null
        @Expose
        @SerializedName("slug")
        var slug: String? = null
    }

    class CoverImage {
        @Expose
        @SerializedName("sizes")
        var sizes: List<Sizes>? = null

        @Expose
        @SerializedName("aspectRatio")
        var aspectRatio: Int = 0
    }

    class Sizes {
        @Expose
        @SerializedName("url")
        var url: String? = null
        @Expose
        @SerializedName("width")
        var width: Int = 0
        @Expose
        @SerializedName("height")
        var height: Int = 0
    }


    class Metadata {
        @Expose
        @SerializedName("totalPages")
        var totalPages: Int = 0
        @Expose
        @SerializedName("page")
        var page: Int = 0
        @Expose
        @SerializedName("perPage")
        var perPage: Int = 0
        @Expose
        @SerializedName("hits")
        var hits: Int = 0
    }
}
