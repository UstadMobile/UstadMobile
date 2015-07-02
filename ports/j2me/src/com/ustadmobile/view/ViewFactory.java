/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.view;

import com.ustadmobile.view.LoginViewJ2ME;
import com.ustadmobile.view.CatalogViewJ2ME;
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
        throw new RuntimeException("Not Implemented");
    }
    
    
    
}
