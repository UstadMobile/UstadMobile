package com.ustadmobile.core.contentformats.xapi

class Result {

    var isCompletion: Boolean = false

    var isSuccess: Boolean = false

    var score: Score? = null

    var duration: String? = null

    var response: String? = null

    var extensions: Map<String, Any>? = null

    inner class Score {

        var scaled: Long = 0

        var raw: Long = 0

        var min: Long = 0

        var max: Long = 0
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val result = o as Result?

        if (isCompletion != result!!.isCompletion) return false
        if (isSuccess != result.isSuccess) return false
        if (if (score != null) score != result.score else result.score != null) return false
        if (if (duration != null) duration != result.duration else result.duration != null)
            return false
        if (if (response != null) response != result.response else result.response != null)
            return false
        return if (extensions != null) extensions == result.extensions else result.extensions == null
    }

    override fun hashCode(): Int {
        var result = if (isCompletion) 1 else 0
        result = 31 * result + if (isSuccess) 1 else 0
        result = 31 * result + if (score != null) score!!.hashCode() else 0
        result = 31 * result + if (duration != null) duration!!.hashCode() else 0
        result = 31 * result + if (response != null) response!!.hashCode() else 0
        result = 31 * result + if (extensions != null) extensions!!.hashCode() else 0
        return result
    }
}
