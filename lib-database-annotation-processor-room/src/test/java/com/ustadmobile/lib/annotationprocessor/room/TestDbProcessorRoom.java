package com.ustadmobile.lib.annotationprocessor.room;

import org.junit.Test;

import java.io.File;

public class TestDbProcessorRoom {

    @Test
    public void testRun() throws Exception{
        DbProcessorRoom dbProcessorRoom = new DbProcessorRoom();
        File destinationDir = new File("/home/mike/src/UstadMobile/app-android/build/generated/source/umdbprocessor");

        dbProcessorRoom.processAllDbClasses(destinationDir);
        dbProcessorRoom.processAllDaos(destinationDir);
    }

}
