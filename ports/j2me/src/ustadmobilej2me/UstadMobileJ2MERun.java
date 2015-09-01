/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ustadmobilej2me;

import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import com.ustadmobile.core.impl.UstadMobileSystemImplFactory;
import javax.microedition.midlet.*;

/**
 * @author varuna
 */
public class UstadMobileJ2MERun extends MIDlet {

    public void startApp() {
        
        Display.init(this);
        UstadMobileSystemImplJ2ME impl = 
                (UstadMobileSystemImplJ2ME)UstadMobileSystemImpl.getInstance();
        Form loadingForm = new Form();
        loadingForm.show();
        impl.handleFormShow(loadingForm);
        impl.startUI();
        
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
}
