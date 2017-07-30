/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.test.port.j2me;
import com.ustadmobile.test.port.j2me.AllTestCases;
import com.ustadmobile.test.port.j2me.TestUtils;
import java.io.IOException;
import java.util.Hashtable;
import com.sun.lwuit.Display;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMUtil;
import com.ustadmobile.test.port.j2me.UstadMobileAppController;
import com.ustadmobile.port.j2me.impl.UMLogJ2ME;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplFactoryJ2ME;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.UMContextGetter;
import j2meunit.framework.AssertionFailedError;
import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestFailure;
import j2meunit.framework.TestResult;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Form;

/**
 * @author varuna
 */
public class UstadMobileTestMidlet extends j2meunit.midletui.TestRunner {
 
    
    private javax.microedition.lcdui.Display lcduiDisplay;
    
    /**
     * Time to show user alerts for in ms.
     */
    private static final int ALERT_DISPLAY_TIME = 4000;
    
    //Start the tests:
    public void startApp(){        
        String testServerURL = 
            "http://" + TestConstants.TEST_SERVER + ":" + TestConstants.TEST_CONTROL_PORT + "/";
        lcduiDisplay = 
            javax.microedition.lcdui.Display.getDisplay(this);
        
        javax.microedition.lcdui.Form connectingForm = new javax.microedition.lcdui.Form("Test");
        connectingForm.append("Connecting....");
        Alert userAlert = new Alert("UstadMobile Tests", 
            "Welcome to the test app: please select 'always' or 'yes' on any security prompts. Starting in 4 seconds", 
            null, AlertType.INFO);
        userAlert.setTimeout(ALERT_DISPLAY_TIME);
        lcduiDisplay.setCurrent(userAlert, connectingForm);
        try { Thread.sleep(ALERT_DISPLAY_TIME); }
        catch(InterruptedException e) {}
        
        Alert connectingAlert = new Alert("Connecting", "Requesting socket: " 
                + testServerURL, null, AlertType.INFO);
        connectingAlert.setTimeout(Alert.FOREVER);

        lcduiDisplay.setCurrent(connectingAlert, connectingForm);
        
        UMLogJ2ME umLog = (UMLogJ2ME)UstadMobileSystemImpl.getInstance().getLogger();
        
        
        if(!umLog.isRemoteSocketConnected()) {
           try {
                String deviceName = "j2metestrun";
                
                int rawPort = UMUtil.requestDodgyHTTPDPort(testServerURL, 
                        "newrawserver", deviceName);
                
                if(rawPort <= 0) {
                    String message = "IOError";
                    if(rawPort == UMUtil.PORT_ALLOC_SECURITY_ERR) {
                        message = "Permission Denied";
                    }
                    
                    quitWithErrorMessage("Error 122", 
                            "Error 122: " + message, null);
                    return;
                }
                
                String socketServer = TestConstants.TEST_SERVER + ":" + rawPort;
                Alert foundPortAlert = new Alert("Connecting", "To socket: " 
                        + socketServer , null , AlertType.INFO);
                lcduiDisplay.setCurrent(foundPortAlert, connectingForm);
                umLog.connectLogToSocket(TestConstants.TEST_SERVER + ":" + rawPort);
                umLog.l(UMLog.INFO, 350, "=====Connected to log server socket=====");
                umLog.l(UMLog.INFO, 350, 
                    UstadMobileSystemImpl.getInstance().getSystemInfo().toString());
                Alert connectedAlert = new Alert("Connected!", "Connected to " 
                        + socketServer + " OK!", null, AlertType.INFO);
                connectedAlert.setTimeout(ALERT_DISPLAY_TIME);
                lcduiDisplay.setCurrent(connectedAlert, connectingForm);
                try { Thread.sleep(ALERT_DISPLAY_TIME); }
                catch(InterruptedException e) {}
            }catch(Exception e) {
                System.err.println("Error connecting to testlog socket");
                e.printStackTrace();
                quitWithErrorMessage("Error 124", 
                        "Exception connecting to test server", e);
                return;
            }
        }
        
        
        UMContextGetter.setContext(this);
        
        Display.init(this);
        
        UstadMobileSystemImpl.getInstance().init(this);
        
        
        
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
                        UstadMobileSystemImpl.getInstance().makeRequest(
                            TestUtils.testSettings.get("testposturl").toString(), 
                            null, testResult);
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
        //notifyDestroyed();
        //destroyApp(bScreenOutput);
    }
    
    void quitWithErrorMessage(String title, String message, Exception e) {
        String exMessage = e != null ? e.toString() : "";
        Alert errorMessage = new Alert(title, message + ":" + exMessage, null, AlertType.ERROR);
        errorMessage.setTimeout(Alert.FOREVER);
        Form errForm = new Form("Exiting automatically");
        lcduiDisplay.setCurrent(errorMessage, errForm);
        try { Thread.sleep(ALERT_DISPLAY_TIME); }
        catch(InterruptedException e2) {}
        destroyApp(false);
        notifyDestroyed();
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
        super.destroyApp(unconditional);
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
