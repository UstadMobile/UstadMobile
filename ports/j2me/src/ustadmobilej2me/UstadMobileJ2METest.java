/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ustadmobilej2me;
import com.ustadmobile.app.tests.AllTestCases;
import com.ustadmobile.app.tests.TestUtils;
import java.io.IOException;
import java.util.Hashtable;
import com.sun.lwuit.Display;
import com.ustadmobile.app.HTTPUtils;
import com.ustadmobile.app.controller.UstadMobileAppController;
import com.ustadmobile.app.tests.TestEPUBRead;
import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestFailure;
import j2meunit.framework.TestResult;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author varuna
 */
public class UstadMobileJ2METest extends j2meunit.midletui.TestRunner {
 
    //Start the tests:
    public void startApp(){
        //start(new String[] { com.ustadmobile.app.tests.AllTestCases.class.getName() });
        
        Display.init(this);
        AllTestCases atc = new AllTestCases() ;
        int ctc = atc.suite().countTestCases();
        start(new String[] {
            atc.getClass().getName()
        });
        
        while(true){
            try{
                int rc = aResult.runCount();              
                int ac = aResult.assertionCount();

                if (rc == ctc){
                    //usually solves the problem of the object getting filled 
                    //and populated on screen.
                    Thread.sleep(500);
                    
                    int numError = aResult.errorCount();
                    String numAssert = String.valueOf(aResult.assertionCount());
                    String numFail = String.valueOf(aResult.failureCount());
                    boolean result = aResult.wasSuccessful();
                    
                    String errorString="";
                    int i=1;
                    for (Enumeration error = aResult.errors(); 
                            error.hasMoreElements(); i++){

                        TestFailure failure = (TestFailure) error.nextElement();
                        errorString = errorString + ""+ (i + ") " + 
                                failure.failedTest());
                        if (failure.thrownException() != null)
                        {
                            errorString = errorString +(failure.thrownException());
                        }      
                    }
                                    
                    if (errorString.length() < 2 ){
                        errorString="No errrors/Failures";
                    }
                    System.out.println("");
                    System.out.println("RESULTS: Error: " + numError + 
                            ", Fail: " + numFail + ", Assert: " + numAssert + 
                            ", Result: " + result + ", Errors/Failures:" + 
                            errorString);
                    /* Return in this format:
                    * POST:
                    *      var numPass = post['numPass'];
                           var numFail = post['numFail'];
                           var logtext = post['logtext'];
                    */
                    Hashtable testResult = new Hashtable();
                    testResult.put("numPass", numAssert);
                    testResult.put("numFail", numFail);
                    testResult.put("logtext", errorString);
                    testResult.put("device", 
                            UstadMobileAppController.getPlatform().toString());
                    
                    try {
                        HTTPUtils.sendPost(
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
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        //Exiting automatically..
        System.out.println("Exiting..");
        notifyDestroyed();
        destroyApp(bScreenOutput);
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
