package com.ustadmobile.lib.contentscrapers;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestExportData {

    @Test
    public void testSqliteDatabase() throws IOException {

        File tmpDir = Files.createTempDirectory("testExportData").toFile();

        ExportData export = new ExportData();
        export.export(tmpDir, 1500);

    }


}
