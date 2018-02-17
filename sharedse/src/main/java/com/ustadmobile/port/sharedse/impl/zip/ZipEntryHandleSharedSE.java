package com.ustadmobile.port.sharedse.impl.zip;

import com.ustadmobile.core.impl.ZipEntryHandle;

import net.lingala.zip4j.model.FileHeader;


/**
 * Created by mike on 8/29/15.
 */
public class ZipEntryHandleSharedSE implements ZipEntryHandle {

    private FileHeader entry;

    public ZipEntryHandleSharedSE(FileHeader entry) {
        this.entry = entry;
    }

    @Override
    public long getSize() {
        return entry.getUncompressedSize();
    }

    @Override
    public String getName() {
        return entry.getFileName();
    }

    @Override
    public boolean isDirectory() {
        return entry.isDirectory();
    }
}
