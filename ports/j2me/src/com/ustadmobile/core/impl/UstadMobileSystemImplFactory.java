/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.impl;

import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;

/*
You would use the system implementation like so:
* ustadMobileSystemImpl = UstadMobileSystemImplFactory.createUstadSystemImpl();
* this would return the J2ME implementation. 
*/
/**
 *
 * @author varuna
 */
public class UstadMobileSystemImplFactory {
    public static UstadMobileSystemImpl createUstadSystemImpl() {

        UstadMobileSystemImplJ2ME ustadMobileSystemImpl = null;
        try {
            ustadMobileSystemImpl = new UstadMobileSystemImplJ2ME();
        }catch(Exception e) {
                   
        }
        return ustadMobileSystemImpl;
    }
    
}
