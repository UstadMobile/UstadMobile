/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.impl;
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
        UstadMobileSystemImplJ2ME ustadMobileSystemImpl = new UstadMobileSystemImplJ2ME();
        return ustadMobileSystemImpl;
        //throw new RuntimeException("Error 42");
    }
    
}
