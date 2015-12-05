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
import com.ustadmobile.port.j2me.view.LoadingForm;
import com.ustadmobile.port.j2me.view.UstadViewFormJ2ME;
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
        
        
        //Uncomment this below to connect to a local or remote log server
        
        /*
        try {
            String serverName = "devserver2.ustadmobile.com";
            int rawPort = UMUtil.requestDodgyHTTPDPort("http://" + serverName + ":8075/", "newrawserver", "forasrun");
            UMLogJ2ME umLog = (UMLogJ2ME)UstadMobileSystemImpl.getInstance().getLogger();
            umLog.connectLogToSocket(serverName + ':' + rawPort);
            umLog.l(UMLog.INFO, 350, "=====Connected to log server socket=====");
            Hashtable systemProps = UstadMobileSystemImpl.getInstance().getSystemInfo();
            String htStr = systemProps.toString();
            umLog.l(UMLog.INFO, 351, htStr);
        }catch(IOException e) {
            System.err.println("Error connecting to testlog socket");
            e.printStackTrace();
        }        
        */
        
        impl.init(this);

        //Uncomment this to send log output to file
        
        /*
        try {
            UMLogJ2ME umLog = (UMLogJ2ME)UstadMobileSystemImpl.getInstance().getLogger();
            umLog.connectLogToSharedDir(this);
        }catch(IOException e) {
            e.printStackTrace();
        }
        */
        
        
        
        try{
            Resources r = Resources.open("/nokia_non_touch_theme.res");
            Hashtable theme = r.getTheme("NokiaTheme");
            UIManager.getInstance().setThemeProps(theme);
            Display.getInstance().setBidiAlgorithm(true);
        }catch(Exception e) {
            impl.getLogger().l(UMLog.CRITICAL, 2, null, e);
        }
        
        
        UstadViewFormJ2ME loadingForm = new LoadingForm(new Hashtable(), this);
        loadingForm.show();
        impl.handleFormShow(loadingForm);
        impl.startUI(this);
        
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
}
