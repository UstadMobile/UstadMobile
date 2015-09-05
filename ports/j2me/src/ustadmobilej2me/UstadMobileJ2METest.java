/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ustadmobilej2me;
import com.ustadmobile.test.port.j2me.AllTestCases;
import com.ustadmobile.test.port.j2me.TestUtils;
import java.io.IOException;
import java.util.Hashtable;
import com.sun.lwuit.Display;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.j2me.app.HTTPUtils;
import com.ustadmobile.port.j2me.app.controller.UstadMobileAppController;
import com.ustadmobile.test.port.j2me.TestEPUBRead;
import j2meunit.framework.AssertionFailedError;
import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestFailure;
import j2meunit.framework.TestResult;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.List;

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
                    Thread.sleep(5000); //5 seconds
                                   
                    int numError = aResult.errorCount();
                    String numAssert = String.valueOf(aResult.assertionCount());
                    String numFail = String.valueOf(aResult.failureCount());
                    boolean result = aResult.wasSuccessful();
                    
                    String errorString="";
                    int i=1;
                    for (Enumeration error = aResult.errors(); 
                            error.hasMoreElements(); i++){
                        result = false; //Failure if errors.
                        TestFailure failure = (TestFailure) error.nextElement();
                        errorString = errorString + '\n' + ""+ (i + ") " + 
                                failure.failedTest());
                        if (failure.thrownException() != null)
                        {
                            errorString = errorString + '\n' + (failure.thrownException());
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
    
    public void addError(Test test, Throwable thrwbl) {
        super.addError(test, thrwbl);
        if(thrwbl != null && thrwbl instanceof Exception){ 
            UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.ERROR,
                110, test.toString(), (Exception)thrwbl);
        }else {
            UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.ERROR,
                110, test.toString());
        }
        
    }

    public void addFailure(Test test, AssertionFailedError afe){
        super.addFailure(test, afe);
        UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.ERROR,
                111, test.toString() + " : " + afe.toString());
    }

    public void endTest(Test test) {
        super.endTest(test);
        UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.INFO, 309, 
                test.toString());
    }

    public void endTestStep(Test test) {
        super.endTestStep(test);
        UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.INFO, 310, 
                test.toString());
    }

    public void startTest(Test test) {
        super.startTest(test);
        UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.INFO, 308, 
                test.toString());
    }
}
