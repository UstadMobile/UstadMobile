/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ustadmobilej2me;

import com.ustadmobile.app.DeviceRoots;
import com.ustadmobile.app.FileUtils;
import com.ustadmobile.app.HTTPUtils;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.midlet.*;

/**
 * @author varuna
 */
public class SetRootWritable extends MIDlet {

    public void startApp() {
        FileConnection fCon = null;
        try {
            DeviceRoots[] allRoots = FileUtils.getAllRoots();
            DeviceRoots mainRoot = FileUtils.getBestRoot();
            HTTPUtils.makeHTTPRequest("http://umcloud1.ustadmobile.com/starting",
                    null, null);
            
            for (int i = 0; i<allRoots.length; i++){
                String path = FileUtils.replaceString(allRoots[i].path, ":/", "//");
                HTTPUtils.makeHTTPRequest(
                    "http://umcloud1.ustadmobile.com/roots/"+path,
                    null, null);
            }
            
            
            String mrp = FileUtils.replaceString(mainRoot.path, ":/", "//");
            HTTPUtils.makeHTTPRequest("http://umcloud1.ustadmobile.com/mainRoot/"+mrp,
                    null, null);
             
             HTTPUtils.makeHTTPRequest("http://umcloud1.ustadmobile.com/makingFileConnection",
                     null, null);
             fCon = (FileConnection) 
                     Connector.open(mainRoot.path, Connector.READ_WRITE);
             HTTPUtils.makeHTTPRequest("http://umcloud1.ustadmobile.com/settingRootWritable",
                     null, null);
             fCon.setWritable(true);
             HTTPUtils.makeHTTPRequest("http://umcloud1.ustadmobile.com/settingRootWritableOK",
                     null, null);
        } catch (IOException ex) {
            try {
                HTTPUtils.makeHTTPRequest("http://umcloud1.ustadmobile.com/EXCEPTION",
                         null, null);
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
            ex.printStackTrace();
        } finally { 
            if (fCon != null){
                try {
                    fCon.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
}
