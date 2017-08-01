/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.impl;

import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;

/**
 *
 * @author mike
 */
public class UstadMobileSystemImplFactory {
    
    public static UstadMobileSystemImpl makeSystemImpl() {
        return new UstadMobileSystemImplJ2ME();
    }
}
