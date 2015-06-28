/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.view;

//import com.ustadmobile.controller.LoginController;
import com.ustadmobile.controller.CatalogController;
import com.ustadmobile.opds.UstadJSOPDSFeed;

/**
 *
 * @author varuna
 */
public interface CatalogView extends UstadView{
    public void setFeed(UstadJSOPDSFeed feed);
    
    public void setController(CatalogController controller);
    
    public void showDialog(String title, String text);
}
