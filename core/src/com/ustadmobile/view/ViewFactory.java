package com.ustadmobile.view;

/**
 * This class needs to be overriden by the implementation to return an 
 * implementation of the view for that platform.  Use the exact same names
 * and methods
 * 
 * 
 * @author mike
 */
public class ViewFactory {
    
    public static LoginView makeLoginView() {
        throw new RuntimeException("Not Implemented");
    };
    
    public static CatalogView makeCatalogView() {
        throw new RuntimeException("Not Implemented");
    }
    
    public static ContainerView makeContainerView() {
        throw new RuntimeException("Not Implemented");
    }
    
}
