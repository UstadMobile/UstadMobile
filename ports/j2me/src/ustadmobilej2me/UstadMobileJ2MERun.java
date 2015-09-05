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
import com.ustadmobile.core.util.UMUtil;
import com.ustadmobile.port.j2me.impl.UMLogJ2ME;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import java.io.IOException;
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
        
        /*
        Uncomment this to connect to a local or remote log server
        */
        
        /*
        try {
            int rawPort = UMUtil.requestDodgyHTTPDPort("http://192.168.0.17:8065/", "newrawserver", "j2merun");
            UMLogJ2ME umLog = (UMLogJ2ME)UstadMobileSystemImpl.getInstance().getLogger();
            umLog.connectLogToSocket("192.168.0.17:" + rawPort);
            umLog.l(UMLog.INFO, 350, "=====Connected to log server socket=====");
            Hashtable systemProps = UstadMobileSystemImpl.getInstance().getSystemInfo();
            String htStr = systemProps.toString();
            umLog.l(UMLog.INFO, 351, htStr);
        }catch(IOException e) {
            System.err.println("Error connecting to testlog socket");
            e.printStackTrace();
        }
        */
        
        try{
            Resources r = Resources.open("/nokia_non_touch_theme.res");
            Hashtable theme = r.getTheme("NokiaTheme");
            UIManager.getInstance().setThemeProps(theme);
        }catch(Exception e) {
            impl.getLogger().l(UMLog.CRITICAL, 2, null, e);
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
