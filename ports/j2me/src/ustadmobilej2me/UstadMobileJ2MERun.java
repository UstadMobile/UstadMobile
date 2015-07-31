/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ustadmobilej2me;

import com.sun.lwuit.Display;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.UstadMobileSystemImplFactory;
import javax.microedition.midlet.*;

/**
 * @author varuna
 */
public class UstadMobileJ2MERun extends MIDlet {

    public void startApp() {
        
        Display.init(this);
        UstadMobileSystemImpl impl = 
                UstadMobileSystemImplFactory.createUstadSystemImpl();
        impl.startUI();
        
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
}
