package com.ustadmobile.core.api.util.forwardheader

/**
 * As per https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Forwarded
 */
data class ForwardHeaderSection(
    val byVal: String?,
    val forVal: String?,
    val hostVal: String?,
    val protoVal: String?
)


fun parseForwardHeader(forwardHeaderLine: String): List<ForwardHeaderSection> {
    val blocks = forwardHeaderLine.split(",").map { it.trim() }
    return blocks.map { blockStr ->
        val blockMap = blockStr.split(";").map {pairStr ->
            val pairVals = pairStr.split("=")

            val valueStr = pairVals[1].trim().let {
                if(it.startsWith("\"")) {
                    it.removePrefix("\"").removeSuffix("\"")
                        .replace("\\\"", "\"")
                }else {
                    it
                }
            }

            Pair(pairVals.first().trim().lowercase(), valueStr)
        }.toMap()

        ForwardHeaderSection(
            byVal = blockMap["by"],
            forVal = blockMap["for"],
            hostVal = blockMap["host"],
            protoVal = blockMap["proto"]
        )
    }
}
