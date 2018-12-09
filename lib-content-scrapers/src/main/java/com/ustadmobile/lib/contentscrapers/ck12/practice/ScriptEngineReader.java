package com.ustadmobile.lib.contentscrapers.ck12.practice;

import java.io.File;
import java.io.FileReader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class ScriptEngineReader {

    private String result = "";

    public String getResult(String code) {

        try {

            FileReader cryptoReader = new FileReader(new File(getClass().getResource("/com/ustadmobile/lib/contentscrapers/ck12/crypto-js.js").toURI()));
            FileReader utils = new FileReader(new File(getClass().getResource("/com/ustadmobile/lib/contentscrapers/ck12/utils.js").toURI()));

            ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
            engine.eval(utils);
            engine.eval(cryptoReader);

            Invocable inv = (Invocable) engine;
            result = (String) inv.invokeFunction("getResult", code);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }
}
