/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ustadmobilej2me;

import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.io.NetworkManager;
import com.sun.lwuit.io.Storage;
import com.ustadmobile.app.forms.HTMLComp;
import com.ustadmobile.app.forms.HTMLForm;
import com.ustadmobile.app.forms.TestForm;
import com.ustadmobile.app.forms.TestForm2;
import com.ustadmobile.core.controller.LoginController;
import javax.microedition.midlet.*;

/**
 * @author varuna
 */
public class LWUITBrowserTest extends MIDlet {


    public void startApp(){
        //Need this for HTML website lookup
        NetworkManager.getInstance().start();
        Storage.init(this);
        
        Display.init(this);
        com.sun.lwuit.Display.getInstance().callSeriallyAndWait(new Runnable() {
            public void run() {
                Form f = new Form();
                f = HTMLForm.loadTestForm();
                f.show();
            }
        });
        try { Thread.sleep(2000); }
        catch(InterruptedException e) {}
        
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
}
