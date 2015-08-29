package com.ustadmobile.port.android.impl.zip;

import com.ustadmobile.core.impl.ZipEntryHandle;
import com.ustadmobile.core.impl.ZipFileHandle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipFile;

/**
 * Created by mike on 8/29/15.
 */
public class ZipFileHandleAndroid implements ZipFileHandle {

    private ZipFile zipFile;

    public ZipFileHandleAndroid(String name) throws IOException {
        zipFile = new ZipFile(name);
    }


    @Override
    public InputStream openInputStream(String name) throws IOException {
        return zipFile.getInputStream(zipFile.getEntry(name));
    }

    @Override
    public ZipEntryHandle getEntry(String name) throws IOException {
        return new ZipEntryHandleAndroid(zipFile.getEntry(name));
    }

    @Override
    public Enumeration entries() throws IOException {
        return new ZipFileHandleEntriesEnumeration();
    }

    @Override
    public void close() throws IOException {
        zipFile.close();
    }

    private class ZipFileHandleEntriesEnumeration implements Enumeration {

        private Enumeration enumeration;

        private ZipFileHandleEntriesEnumeration() {
            enumeration = ZipFileHandleAndroid.this.zipFile.entries();
        }

        @Override
        public boolean hasMoreElements() {
            return enumeration.hasMoreElements();
        }

        @Override
        public Object nextElement() {
            return enumeration.nextElement();
        }
    }
}
