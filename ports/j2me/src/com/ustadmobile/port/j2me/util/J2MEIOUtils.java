/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.util;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import java.io.IOException;
import javax.microedition.io.Connection;

/**
 *
 * @author mike
 */
public class J2MEIOUtils {
    
    public static final void closeConnection(Connection con) {
        if(con != null) {
            try {
                con.close();
            }catch(IOException e) {
                UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.INFO, 101, 
                    null, e);
            }
        }
    }
    
}
