package com.ustadmobile.lib.contentscrapers.ck12.practice

import java.io.File
import java.io.FileReader

import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class ScriptEngineReader {

    private var result = ""

    fun getResult(code: String): String {

        try {

            val cryptoReader = FileReader(File(javaClass.getResource("/com/ustadmobile/lib/contentscrapers/ck12/crypto-js.js").toURI()))
            val utils = FileReader(File(javaClass.getResource("/com/ustadmobile/lib/contentscrapers/ck12/utils.js").toURI()))

            val engine = ScriptEngineManager().getEngineByExtension("js")
            engine.eval(utils)
            engine.eval(cryptoReader)

            val inv = engine as Invocable
            result = inv.invokeFunction("getResult", code) as String

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result

    }
}
