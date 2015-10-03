/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.view;

import com.ustadmobile.port.j2me.view.LoginViewJ2ME;
import com.ustadmobile.port.j2me.view.CatalogViewJ2ME;
import com.ustadmobile.port.j2me.view.ContainerViewJ2ME;
/**
 *
 * @author varuna
 */
public class ViewFactory  { 
    
    public ViewFactory(){
        
    }
    
    public static LoginView makeLoginView() {
        return new LoginViewJ2ME();
    }
    
    public static CatalogView makeCatalogView() {
       return new CatalogViewJ2ME();
    }
    
    public static ContainerView makeContainerView() {
        return new ContainerViewJ2ME();
    }
    
    
    
}
