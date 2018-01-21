package com.ustadmobile.lib.database.annotationprocessor;

import com.ustadmobile.lib.dbprocessor.EntityProcessorRoom;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by mike on 1/21/18.
 */

public class TestEntityProcess {

    @Test
    public void testProcess() throws IOException{
        EntityProcessorRoom.processorDir(
                new File("/home/mike/src/UstadMobile/lib-database/src/main/java/com/ustadmobile/lib/db/entities"),
                new File("/home/mike/tmp/entities"));
    }

}
