/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.test.port.j2me;

import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.j2me.view.ContainerViewPageSplitter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import com.ustadmobile.core.util.TestUtils;

/**
 *
 * @author mike
 */
public class TestPageSplitter extends j2meunit.framework.TestCase{
    
    private String httpRoot;
    
    public TestPageSplitter() {
        setName("TestPageSplitter");
    }
    
    protected void setUp() throws Exception {
        super.setUp(); 
        httpRoot = TestUtils.getInstance().getHTTPRoot();
    }
    
    public void runTest() throws Throwable{
        Throwable t = null;
        
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String fileURI = null;
        String httpURL = httpRoot +"page-to-split.xhtml";
        InputStream fin = null;
        
        try {
            fileURI = UMFileUtil.joinPaths(new String[] {
                impl.getSharedContentDir(), "page-to-split.xhtml"});
            if(impl.fileExists(fileURI)) {
                impl.removeFile(fileURI);
            }
            
            //copy the file somewhere we can use it
            HttpConnection httpCon = (HttpConnection)Connector.open(httpURL);
            InputStream httpIn = httpCon.openInputStream();
            OutputStream fout = impl.openFileOutputStream(fileURI, 0);
            UMIOUtils.readFully(httpIn, fout, 1024);
            httpIn.close();
            fout.close();
            httpCon.close();
            
            int[] startingPos = new int[]{0, 0};
            do {
                fin = impl.openFileInputStream(fileURI);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                startingPos = ContainerViewPageSplitter.dividePage(fin, bout, 
                    UstadMobileConstants.MICRO_ED_PAGESPLIT_TEXTLEN, 200000, startingPos[0], startingPos[1]);
                fin.close();
            }while(startingPos[0] != ContainerViewPageSplitter.POS_FINAL_SECTION);
        }catch(Throwable t2) {
            t = t2;
        }finally {
            UMIOUtils.closeInputStream(fin);
            if(impl.fileExists(fileURI)) {
                impl.removeFile(fileURI);
            }
            if(t != null) {
                throw t;
            }
        }
    }
    
}
