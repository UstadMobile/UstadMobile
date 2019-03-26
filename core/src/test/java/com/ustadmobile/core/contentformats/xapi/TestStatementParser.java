package com.ustadmobile.core.contentformats.xapi;

import com.ustadmobile.core.util.UMIOUtils;

import org.junit.Test;

import java.io.IOException;

public class TestStatementParser {

    public final String contextWithObject = "/com/ustadmobile/core/contentformats/xapi/contextWitObject";
    public final String fullstatement = "/com/ustadmobile/core/contentformats/xapi/fullstatement";
    public final String simpleStatement = "/com/ustadmobile/core/contentformats/xapi/simpleStatment";
    public final String subStatement = "/com/ustadmobile/core/contentformats/xapi/substatement";

    @Test
    public void givenValidStatement_parseAll() throws IOException {

        Statement statement = Statement.loadObject(UMIOUtils.readStreamToString(getClass().getResourceAsStream(simpleStatement)));

    }

    @Test
    public void givenValidStatement_parseContext() throws IOException {

        Statement statement = Statement.loadObject(UMIOUtils.readStreamToString(getClass().getResourceAsStream(contextWithObject)));

    }

    @Test
    public void givenValidStatement_parseFull() throws IOException {

        Statement statement = Statement.loadObject(UMIOUtils.readStreamToString(getClass().getResourceAsStream(fullstatement)));

    }

    @Test
    public void givenValidStatement_parseSub() throws IOException {

        Statement statement = Statement.loadObject(UMIOUtils.readStreamToString(getClass().getResourceAsStream(subStatement)));

    }


}
