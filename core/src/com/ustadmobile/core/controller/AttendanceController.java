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
package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.omr.OMRRecognizer;
import com.ustadmobile.core.view.AttendanceView;
import com.ustadmobile.core.view.UstadView;
import java.io.InputStream;
import jp.sourceforge.qrcode.QRCodeDecoder;
import jp.sourceforge.qrcode.data.QRCodeImage;

/**
 *
 * @author mike
 */
public class AttendanceController extends UstadBaseController{

    //areas in which optical marks are to be found on the paper
    public static final float AREA_WIDTH = 607f;
    
    public static final float AREA_HEIGHT = 902f;
    
    public static final float OMR_AREA_OFFSET_X = (311f/AREA_WIDTH);
    
    public static final float OMR_AREA_OFFSET_Y = (37.5f/AREA_HEIGHT);
    
    public static final float OM_WIDTH = 26f/AREA_WIDTH;
    
    public static final float OM_HEIGHT = 20f/AREA_HEIGHT;
    
    public static final float OM_ROW_HEIGHT = 25.8f/AREA_HEIGHT;

    
    private AttendanceView view;
    
    public AttendanceController(Object context) {
        super(context);
    }
    
    public static AttendanceController makeControllerForView(AttendanceView view) {
        AttendanceController ctrl = new AttendanceController(view.getContext());
        ctrl.setView(view);
        return ctrl;
    }
    
    public void setUIStrings() {
        
    }

    public void setView(UstadView view) {
        super.setView(view); 
        this.view = (AttendanceView)view;
    }
    
    
    
    public void handleClickSnap() {
        view.showTakePicture();
    }
    
    /**
     * Handle when the image has been taken by the underlying system
     * 
     */
    public void handlePictureAcquired(Object sysImage) {
        InputStream fileIn = null;
        try {
            QRCodeDecoder decoder = new QRCodeDecoder();
            QRCodeImage img = UstadMobileSystemImpl.getInstance().getQRCodeImage(
                sysImage);
            boolean[][] bitmapImg = OMRRecognizer.convertImgToBitmap(img);
            img = null;
            
            boolean[][] marks = OMRRecognizer.getMarks(bitmapImg,
                OMR_AREA_OFFSET_X, OMR_AREA_OFFSET_Y, 
                OM_WIDTH, OM_HEIGHT, OM_ROW_HEIGHT, 
                4, 33, null);
            
            System.out.println("We recognized it");
        }catch(Exception e) {
            System.out.println("we didn't jim");
        }
    }
    
    
}
