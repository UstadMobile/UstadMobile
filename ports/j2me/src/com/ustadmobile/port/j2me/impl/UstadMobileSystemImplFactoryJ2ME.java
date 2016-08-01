/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.impl;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.UstadMobileSystemImplFactory;

/**
 *
 * @author mike
 */
public class UstadMobileSystemImplFactoryJ2ME extends UstadMobileSystemImplFactory {

    public UstadMobileSystemImpl makeUstadSystemImpl() {
        return new UstadMobileSystemImplJ2ME();
    }
    
}
