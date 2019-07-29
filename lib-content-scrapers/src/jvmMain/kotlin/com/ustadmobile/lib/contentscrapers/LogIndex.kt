package com.ustadmobile.lib.contentscrapers

class LogIndex {

    var title: String? = null

    var entries: List<IndexEntry>? = null

    var links: Map<String, String>? = null

    class IndexEntry {

        var url: String? = null

        var mimeType: String? = null

        var path: String? = null

        var headers: Map<String, String>? = null

        var requestHeaders: Map<String, String>? = null

    }

}
