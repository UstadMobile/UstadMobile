/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ustadmobilej2me;
import com.sun.lwuit.Form;
import com.ustadmobile.app.tests.AllTestCases;
import com.ustadmobile.app.tests.TestUtils;
import java.io.IOException;
import java.util.Hashtable;
import com.sun.lwuit.Display;
import com.ustadmobile.app.HTTPUtils;
import com.ustadmobile.app.RMSUtils;
import com.ustadmobile.app.controller.UstadMobileAppController;
import com.ustadmobile.controller.LoginController;

//TextBox for screen:
import javax.microedition.lcdui.TextBox;

/**
 * @author varuna
 */
public class Midlet extends j2meunit.midletui.TestRunner {

    private TextBox tbox;
    
    public Midlet() {
        tbox = new TextBox("App Dir", 
                "Hows it going?", 100, 0 );
    }
    
    //To Display on Screen
    public void startApp(){
        Display.init(this);                   
        /*
        Form f = new Form("Hello there");
        //Form f = TestForm.loadTestForm();
        f.show();
        */
        
        LoginController loginController = new LoginController();
        loginController.show();
    }
    
    //Start the tests:
    public void _startApp(){
        //start(new String[] { com.ustadmobile.app.tests.AllTestCases.class.getName() });
        
        
        Display.init(this);
        AllTestCases atc = new AllTestCases() ;
        int ctc = atc.suite().countTestCases();
        System.out.println("Number of Tests: " + ctc);
        start(new String[] {
            atc.getClass().getName()
        });
        
        while(true){
            try{
                int rc = aResult.runCount();
                int ac = aResult.assertionCount();
                System.out.println("assertionCount: " + ac + "/" + rc);

                if (rc == ctc){
                    System.out.println("");
                    System.out.println("All done?");
                    int numError = aResult.errorCount();
                    String numAssert = String.valueOf(aResult.assertionCount());
                    String numFail = String.valueOf(aResult.failureCount());
                    boolean result = aResult.wasSuccessful();
                    System.out.println("Error: " + numError + ", Fail: " + 
                            numFail + ", Assert: " + numAssert + 
                            ", Result: " + result);

                    /* Return in this format:
                    * POST:
                    *      var numPass = post['numPass'];
                           var numFail = post['numFail'];
                           var logtext = post['logtext'];
                    */
                    Hashtable testResult = new Hashtable();
                    testResult.put("numPass", numAssert);
                    testResult.put("numFail", numFail);
                    testResult.put("logtext", 
                            "Result");
                    testResult.put("device", 
                            UstadMobileAppController.getPlatform().toString());
                    try {
                        String postResult = null;

                        postResult = HTTPUtils.sendPost(
                            TestUtils.testSettings.get("testposturl").toString(), 
                                testResult);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    //Get outta here.
                    break;
                }
            }catch(Exception e){
                System.out.print(".");
            }       
        }
    }
    
    /**
     * Helpful for executing tests from command line / microemulator
     */
    public static void main(String[] args){
        //j2meunit.textui.TestRunner.main(new String[] 
            //{com.ustadmobile.app.tests.AllTestCases.class.getName()});
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
}
