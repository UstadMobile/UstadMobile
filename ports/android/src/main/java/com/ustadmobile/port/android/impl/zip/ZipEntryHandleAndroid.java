package com.ustadmobile.port.android.impl.zip;

import com.ustadmobile.core.impl.ZipEntryHandle;
import java.util.zip.ZipEntry;

/**
 * Created by mike on 8/29/15.
 */
public class ZipEntryHandleAndroid implements ZipEntryHandle {

    private ZipEntry entry;

    public ZipEntryHandleAndroid(ZipEntry entry) {
        this.entry = entry;
    }

    @Override
    public long getSize() {
        return entry.getSize();
    }

    @Override
    public String getName() {
        return entry.getName();
    }

    @Override
    public boolean isDirectory() {
        return entry.isDirectory();
    }
}
