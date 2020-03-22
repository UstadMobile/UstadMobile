package com.ustadmobile.lib.contentscrapers.ck12.practice

import com.ustadmobile.lib.contentscrapers.UMLogUtil
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.exception.ExceptionUtils
import java.io.*
import java.nio.charset.StandardCharsets.UTF_8

import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class ScriptEngineReader {

    private var result = ""

    fun getResult(code: String): String {
        var cryptoInput: InputStream? = null
        var utilsInput: InputStream? = null

        var cryptoReader: InputStreamReader? = null
        var utilsReader: InputStreamReader? = null


        try {
            cryptoInput = javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/ck12/crypto-js.js")
            cryptoReader = InputStreamReader(cryptoInput!!, UTF_8)

            utilsInput = javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/ck12/utils.js")
            utilsReader = InputStreamReader(utilsInput!!, UTF_8)

            val engine = ScriptEngineManager().getEngineByExtension("js")
            engine.eval(utilsReader)
            engine.eval(cryptoReader)

            val inv = engine as Invocable
            result = inv.invokeFunction("getResult", code) as String

        } catch (e: Exception) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Failed to read answer from encryption for")
        }finally {
            utilsReader?.close()
            utilsInput?.close()

            cryptoReader?.close()
            cryptoInput?.close()
        }

        return result

    }
}
