package com.ustadmobile.lib.contentscrapers.CK12;

import java.io.File;
import java.io.FileReader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class Rhino {

    private String result = "";

    public String getResult(String code) {

        try {

            FileReader cryptoReader = new FileReader(new File(getClass().getResource("/com/ustadmobile/lib/contentscrapers/crypto-js.js").toURI()));
            FileReader utils = new FileReader(new File(getClass().getResource("/com/ustadmobile/lib/contentscrapers/utils.js").toURI()));

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
