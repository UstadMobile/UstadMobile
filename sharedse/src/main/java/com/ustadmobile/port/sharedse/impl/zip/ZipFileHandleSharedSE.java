package com.ustadmobile.port.sharedse.impl.zip;

import com.ustadmobile.core.impl.ZipEntryHandle;
import com.ustadmobile.core.impl.ZipFileHandle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;


/**
 * Created by mike on 8/29/15.
 */
public class ZipFileHandleSharedSE implements ZipFileHandle {

    private ZipFile zipFile;

    public ZipFileHandleSharedSE(String name) throws IOException{
        try {
            zipFile = new ZipFile(name);
        }catch(ZipException e) {
            throw new IOException(e);
        }
    }

    public ZipFileHandleSharedSE(ZipFile zipFile){
        this.zipFile = zipFile;
    }

    @Override
    public InputStream openInputStream(String name) throws IOException {
        try {
            FileHeader header = zipFile.getFileHeader(name);
            if(header != null){
                return zipFile.getInputStream(header);
            }else {
                return null;
            }
        }catch(ZipException e) {
            throw new IOException(e);
        }


    }

    @Override
    public ZipEntryHandle getEntry(String name) throws IOException {
        try {
            FileHeader entry = zipFile.getFileHeader(name);
            if(entry != null) {
                return new ZipEntryHandleSharedSE(entry);
            }else {
                return null;
            }
        }catch(ZipException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Enumeration entries() throws IOException {
        return new ZipFileHandleEntriesEnumeration();
    }

    @Override
    public void close() throws IOException {
        zipFile = null;
    }

    private class ZipFileHandleEntriesEnumeration implements Enumeration {

        private Iterator<FileHeader> enumeration;

        private ZipFileHandleEntriesEnumeration() {
            try {
                enumeration = ZipFileHandleSharedSE.this.zipFile.getFileHeaders().iterator();
            }catch(ZipException e) {
                e.printStackTrace();
            }

        }

        @Override
        public boolean hasMoreElements() {
            return enumeration.hasNext();
        }

        @Override
        public Object nextElement() {
            return new ZipEntryHandleSharedSE(enumeration.next());
        }
    }
}
