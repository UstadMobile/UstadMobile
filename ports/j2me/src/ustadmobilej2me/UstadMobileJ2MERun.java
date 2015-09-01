/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ustadmobilej2me;

import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import java.util.Hashtable;
import javax.microedition.midlet.*;

/**
 * @author varuna
 */
public class UstadMobileJ2MERun extends MIDlet {

    public void startApp() {
        
        Display.init(this);
        
        UstadMobileSystemImplJ2ME impl = 
                (UstadMobileSystemImplJ2ME)UstadMobileSystemImpl.getInstance();
        
        try{
            Resources r = Resources.open("/nokia_non_touch_theme.res");
            Hashtable theme = r.getTheme("NokiaTheme");
            UIManager.getInstance().setThemeProps(theme);
        }catch(Exception e) {
            impl.getLogger().l(UMLog.CRITICAL, 400, null, e);
        }
        
        
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
