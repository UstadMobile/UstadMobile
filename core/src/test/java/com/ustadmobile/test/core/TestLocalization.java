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

import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.LocaleUtil;
import com.ustadmobile.core.util.MessagesHashtable;
import java.io.IOException;
import java.io.InputStream;


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
public class TestLocalization extends ActivityInstrumentationTestCase2<UstadMobileActivity> {
 $else$ */
public abstract class TestLocalization extends TestCase{
/* $endif$ */

    public TestLocalization() {
        /* $if umplatform == 1 $ 
        super(UstadMobileActivity.class);
        $endif */
    }
    
    public void testLocalization() throws IOException {
        Object context = UMContextGetter.getContext(this);
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        //test substitutions
        assertEquals("Handle normal sub", "download 10 entries?",
                LocaleUtil.formatMessage("download %s entries?", "10"));
        assertEquals("Handle sub at beginning", "10: the number to download?",
                LocaleUtil.formatMessage("%s: the number to download?", "10"));
        assertEquals("Handle sub at end", "the number to download is: 10",
                LocaleUtil.formatMessage("the number to download is: %s", "10"));
        
        
        InputStream in = impl.openResourceInputStream("locale/en.properties", context);
        assertNotNull("Can load resource input stream", in);
        MessagesHashtable mht = MessagesHashtable.load(in);
        assertNotNull("Loaded hashtable", mht);
        in.close();
        assertEquals("Loaded first message correct", "Login", 
                mht.get(MessageIDConstants.login));
    }
    
    public void runTest() throws IOException {
        testLocalization();
    }
}
    

