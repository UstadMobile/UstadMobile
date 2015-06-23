/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.model;
import com.ustadmobile.opds.*;

/**
 *
 * @author mike
 */
public class CatalogModel {
    
    public UstadJSOPDSFeed opdsFeed;
    
    public CatalogModel(UstadJSOPDSFeed opdsFeed) {
        this.opdsFeed = opdsFeed;
    }
    
}
