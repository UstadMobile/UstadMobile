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
package com.ustadmobile.test.core;

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.omr.OMRRecognizer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import jp.sourceforge.qrcode.QRCodeDecoder;
import jp.sourceforge.qrcode.data.QRCodeImage;

/* $if umplatform == 2  $
    import com.ustadmobile.test.port.j2me.TestCase;
 $else$ */
    import junit.framework.TestCase;
/* $endif$ */


/* $if umplatform == 1 $
        import android.test.ActivityInstrumentationTestCase2;
        import com.toughra.ustadmobile.UstadMobileActivity;
 $endif$ */

/**
 *
 * @author mike
 */
/* $if umplatform == 1  $
public class TestOMR extends ActivityInstrumentationTestCase2<UstadMobileActivity>{
 $else$ */
public class TestOMR extends TestCase {
/* $endif */

    private byte[] imgBytes;
    
    public TestOMR() {
        /* $if umplatform == 1 $ 
        super(UstadMobileActivity.class);
        $endif */
    }

    protected void setUp() throws Exception {
        String imgURL = TestUtils.getInstance().getHTTPRoot() + "omr-img1.jpg";
        HTTPResult imgResult = UstadMobileSystemImpl.getInstance().makeRequest(
            imgURL, null, null);
        imgBytes = imgResult.getResponse();
    }
    
    public void testOMR() throws IOException{
        //required to init the debug canvas adapter - otherwise subsequent calls throw nullpointerexception
        QRCodeDecoder decoder = new QRCodeDecoder();
        
        QRCodeImage img = UstadMobileSystemImpl.getInstance().getQRCodeImage(
            new ByteArrayInputStream(imgBytes));
        assertNotNull("Can get QR Code image from stream", img);
        boolean[][] bitmap = OMRRecognizer.convertImgToBitmap(img);
        img = null;
        
        boolean[][] omrResult = OMRRecognizer.getMarks(bitmap, 
            OMRRecognizer.OMR_AREA_OFFSET_X, OMRRecognizer.OMR_AREA_OFFSET_Y, 
            OMRRecognizer.OM_WIDTH, OMRRecognizer.OM_HEIGHT, OMRRecognizer.OM_ROW_HEIGHT, 
            4, 33, null);
    }
    
}
