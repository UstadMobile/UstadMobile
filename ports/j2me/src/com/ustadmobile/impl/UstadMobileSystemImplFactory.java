/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.impl;

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
