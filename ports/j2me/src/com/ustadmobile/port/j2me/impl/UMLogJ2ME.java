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


    public void l(int level, int code, String message) {
        System.err.println(code + " : " + message);
    }

    public void l(int level, int code, String message, Exception exception) {
        System.err.println(code + " : " + message + " : " + exception.toString());
    }

    public void l(int level, String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
