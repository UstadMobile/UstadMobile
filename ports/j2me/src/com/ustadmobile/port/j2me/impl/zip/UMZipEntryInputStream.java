/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.impl.zip;

import com.ustadmobile.core.impl.ZipEntryHandle;
import gnu.classpath.java.io.FilterInputStream;
import gnu.classpath.java.util.zip.ZipInputStream;

/**
 *
 * @author mike
 */
public class UMZipEntryInputStream extends FilterInputStream {
    
    private ZipEntryHandle entryHandle;
    
    public UMZipEntryInputStream(ZipInputStream zin, ZipEntryHandle entryHandle) {
        super(zin);
        this.entryHandle = entryHandle;
    }
    
    public ZipEntryHandle getZipEntry() {
        return entryHandle;
    }
    
}
