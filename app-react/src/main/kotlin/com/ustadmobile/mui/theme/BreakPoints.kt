@Suppress("EnumEntryName")
enum class Breakpoint {
    xs, sm, md, lg, xl
}

external interface Breakpoints {
    var keys: Array<Breakpoint>

    @JsName("up")
    fun upWithMediaTerm(key: String): String

    @JsName("up")
    fun upWithMediaTerm(key: Int): String

    @JsName("down")
    fun downWithMediaTerm(key: String): String

    @JsName("down")
    fun downWithMediaTerm(key: Int): String

    @JsName("between")
    fun betweenWithMediaTerm(start: String, end: String): String

    @JsName("only")
    fun onlyWithMediaTerm(key: String): String

    @JsName("width")
    fun widthWithStringKey(key: String): Int
}

private fun removeMediaString(query: String) = if (query.startsWith("@media")) query.substring(6) else query

fun Breakpoints.up(key: Breakpoint): String {
    return removeMediaString(upWithMediaTerm(key.toString()))
}

fun Breakpoints.down(key: Breakpoint): String {
    return removeMediaString(downWithMediaTerm(key.toString()))
}

fun Breakpoints.between(startKey: Breakpoint, endKey: Breakpoint): String {
    return removeMediaString(betweenWithMediaTerm(startKey.toString(), endKey.toString()))
}

fun Breakpoints.only(key: Breakpoint): String {
    return removeMediaString(onlyWithMediaTerm(key.toString()))
}

fun Breakpoints.width(key: Breakpoint): Int {
    return widthWithStringKey(key.toString())
}
