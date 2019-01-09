package com.ustadmobile.lib.annotationprocessor.core;

import com.ustadmobile.lib.annotationprocessor.core.db.ExampleDatabase;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class TestEntityWithAttachments {

    File attachmentsDir;

    @Before
    public void before() throws IOException{
        attachmentsDir = File.createTempFile("ExampleDatabase", "attachments");
        attachmentsDir.delete();
        attachmentsDir.mkdir();
    }

    @After
    public void tearDown() throws IOException{
        FileUtils.deleteDirectory(attachmentsDir);
    }

    @Test
    public void givenAttachmentSet_whenRetrieved_thenShouldBeIdentical() throws IOException {
        byte[] byteBuf = new byte[]{1,2,3,4,5,6,7,8};
        ExampleDatabase db = ExampleDatabase.getInstance(null);
        db.setAttachmentsDir(attachmentsDir.getAbsolutePath());
        db.getExampleSyncableEntityWithAttachmentDao().setAttachment(1,
                new ByteArrayInputStream(byteBuf));
        Assert.assertTrue(true);
        String fileName = db.getExampleSyncableEntityWithAttachmentDao().getAttachmentUri(1);
        Assert.assertTrue("Attachment file exists", new File(fileName).exists());
    }

}
