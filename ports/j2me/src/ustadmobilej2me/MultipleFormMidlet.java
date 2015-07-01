/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ustadmobilej2me;

import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.ustadmobile.app.forms.TestForm;
import com.ustadmobile.app.forms.TestForm2;
import com.ustadmobile.controller.LoginController;
import javax.microedition.midlet.*;

/**
 * @author varuna
 */
public class MultipleFormMidlet extends MIDlet {


    public void startApp(){
        Display.init(this);
        com.sun.lwuit.Display.getInstance().callSeriallyAndWait(new Runnable() {
            public void run() {
                Form f = new Form();
                f = TestForm.loadTestForm();
                f.show();
                f.setTitle("in the Thread..");
            }
        });
        try { Thread.sleep(2000); }
        catch(InterruptedException e) {}
        
        final LoginController loginController = new LoginController();
        com.sun.lwuit.Display.getInstance().callSeriallyAndWait(new Runnable() {
            public void run() {
                loginController.show();
                /*
                Form f = new Form();
                f = TestForm2.loadTestForm();
                f.show();
                f.setTitle("in the thread2..");
                * */
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
