package com.ustadmobile.core.impl;


public class UstadMobileSystemImplFactory {

    public static Object makeSystemImpl(){
        throw new RuntimeException("The base system impl factory must be overriden");
    }
}