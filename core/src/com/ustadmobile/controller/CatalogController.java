/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.controller;

import com.ustadmobile.impl.UstadMobileSystemImpl;
import com.ustadmobile.view.CatalogView;

/**
 *
 * @author varuna
 */
public class CatalogController {
    
    private CatalogView view;
    
    public CatalogController() {
        
    }
    
    //methods go here..
    
    public void handleClickRefresh() {
        
    }
    
    public void show() {
        this.view = (CatalogView)
                UstadMobileSystemImpl.getInstance().makeView("Catalog");
    }
    
    public void hide() {
        
    }
}
