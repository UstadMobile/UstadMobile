/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ustadmobile.port.j2me.impl;

import com.ustadmobile.core.impl.UMLog;

/**
 *
 * @author mike
 */
public class UMLogJ2ME extends UMLog{

    public UMLogJ2ME() {
        
    }
    
    

    public void l(int level, int code, String message) {
        System.out.print("[");
        System.out.print(new java.util.Date().toString());
        System.out.print("]");
        System.out.println("code:" + code + " : " + message);
    }

    public void l(int level, int code, String message, Exception exception) {
        System.out.print("[");
        System.out.print(new java.util.Date().toString());
        System.out.print("]");
        System.out.println("code:" + code + " : " + message + " : " + exception.toString());
    }

}