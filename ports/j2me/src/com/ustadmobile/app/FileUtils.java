/*
Code taken from Research In Motion's Facebook SDK : FileUtils.java
*/

/*
The reason why we want to generate according to the roots is because, 
fileconn.dir.photos is not everywhere. 
Instead we have to get the phone memory root and save and search for test 
settings over there. 
Also not every phone will have an SD card. 

So as per looking at J2ME so far:
Assume no SD card. 
Assume no Photos dir: fileconn.dir.photos
Assume saving to Phone Memory is not always allowed.

try 1:  Root
try 2: Photos
try 3: Images

*/

package com.ustadmobile.app;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.amms.control.PriorityControl;
import javax.microedition.io.Connector;
import javax.microedition.io.OutputConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;


public class FileUtils {
    protected static final String FILE_PREFIX = "file:///";
    private static final String SDCARD_STRING = "SDCard";
    /** The File seperator character */
    public static char FILE_SEP = '/';
    public static final String USTAD_CONTENT_DIR = "ustadmobileContent";

    //The fileURI?
    //String fileURI;
    /*
    * Creates the File, Dir.
    *fileName should start with "file:///"
    *
    */
    public static boolean createRecursively(String fileName, int mode, 
        boolean isDir) throws IOException {

        boolean created = false;
        boolean parentCreated = false;

        if ((fileName == null) || fileName.equals("") || 
            !fileName.trim().startsWith(FILE_PREFIX)) {
            // do nothing
        } else {

            fileName = fileName.trim();

            if (isRoot(fileName)) {
                created = true;
            } else {
                parentCreated = createRecursively(
                        parentOf(fileName), mode, true);
                if (parentCreated) {
                        created = createFileOrDir(fileName, 
                                mode, isDir);
                }
            }

        }

        return created;

    }
    
    public static DeviceRoots getFirstRoot(){
        DeviceRoots bestRoot = new DeviceRoots();
        bestRoot = null;
        try{
            DeviceRoots[] deviceRoots = getAllRoots();
            if (deviceRoots != null) {
                bestRoot = deviceRoots[0];
                return bestRoot;
            }
        }catch(Exception e){
            return null;
        }
        return null;
    }
    
    public static DeviceRoots getBestRoot(){
        DeviceRoots bestRoot = new DeviceRoots();
        bestRoot = null;
        DeviceRoots[] deviceRoots = getAllRoots();
        long startSize = 0;
        for (int i = 0; i<deviceRoots.length; i++){
            if (deviceRoots[i].availableSize > startSize){
                bestRoot = deviceRoots[i];
                startSize = deviceRoots[i].availableSize;
            }
        }
        if(bestRoot != null){
            return bestRoot;
        }
        return null;
    }

    public static DeviceRoots[] getAllRoots(){
        Enumeration enu = null;
        
        try{
            enu = FileSystemRegistry.listRoots();
            Vector roots = new Vector();
            DeviceRoots root = new DeviceRoots();
            while (enu.hasMoreElements()){
                root = new DeviceRoots();
                String rootElement = (String)enu.nextElement();
                rootElement = rootElement.trim();
                String rootName = rootElement.substring(0, 
                        rootElement.length() - 1);
                root.name = rootName;
                if (SDCARD_STRING.equals(rootName)){
                        System.out.println("SDCARD found!");
                    }
                String baseFolder = FILE_PREFIX + rootElement;
                root.path = baseFolder;
                try{
                    FileConnection fCon = (FileConnection)Connector.open(
                            baseFolder, Connector.READ_WRITE);
                    try{
                        long rootSize = fCon.availableSize();
                        root.availableSize = rootSize;
                    }catch (Exception rs){
                        rs.printStackTrace();
                        System.out.println("Unable to "
                                + "calculate root size for root: " + baseFolder);
                    }
                    
                    //return("Able to 
                    //access this folder amd READ amd WRITE to it. ");
                    String umFolder = baseFolder + "/testfolder";
                    roots.addElement(root);
                    fCon.close();
  
                }catch (Exception b){
                    b.printStackTrace();
                    System.out.println("Unable to open: " + baseFolder);
                }
            }
            DeviceRoots deviceRoots[] = new DeviceRoots[roots.size()];
            roots.copyInto(deviceRoots);
            return deviceRoots;
            
            //return ("Unable to find roots");
        }catch (Exception lre){
            lre.printStackTrace();
            System.out.println("listRoots exception");
        }
        return null;
        
    }

    public static boolean createFileInRoot(String fileName){
            boolean created  = false;
            if ((fileName == null) || fileName.equals("") ){
                    // do nothing
            } else {

                    try {
                  FileConnection filecon = (FileConnection)
                          Connector.open(fileName);
                  // Always check whether the file or directory exists.
                  // Create the file if it doesn't exist.
                  if(!filecon.exists()) {
                     filecon.create();
                  }
                  filecon.close();
               } catch(IOException ioe) {
               }

            }
            return created;
    }
    
    protected static boolean checkDir(String dirName){
        try{
            dirName = dirName.trim();
            if (!dirName.endsWith("/")){
                dirName += "/";
            }
            try{
                FileConnection fc = (FileConnection) Connector.open(dirName, 
                    Connector.READ_WRITE);
                if (fc.exists()){
                    return true;
                }
                return false;
            }catch(Exception e) {}
            return false;
        }catch(Exception e){}
        return false;
    }

    protected static boolean createFileOrDir(String fileName, int mode, 
            boolean isDir) throws IOException {

            boolean created = false;
            /*
            try {
            * */
            fileName = fileName.trim();

            if (isDir && !fileName.endsWith("/")) {
                    fileName += "/";
            }

            FileConnection fc = (FileConnection) Connector.open(fileName, mode);

            if (fc.exists()) {
                    created = true;
            } else {
                    if (isDir) {
                            fc.mkdir();
                    } else {
                            fc.create();
                    }
                    created = true;
            }

            /*
            } catch (Throwable t) {
                    t.printStackTrace();
            } finally {
            }
            * */
            return created;
            
    }

    public static String parentOf(String inStr) {
            String result = null;

            if ((inStr != null) && !inStr.trim().equals("")) {
                    inStr = inStr.trim();
                    int index = inStr.lastIndexOf('/');
                    if (index != -1) {
                            result = inStr.substring(0, index);
                    }
            }

            return result;
    }

    public static boolean isRoot(String pFileName) {
            boolean output = false;
            Enumeration e = FileSystemRegistry.listRoots();
            String fileName = pFileName.trim() + "/";

            while (e.hasMoreElements()) {
                    String thisRoot = (String) e.nextElement();
                    output = fileName.equals(FILE_PREFIX + thisRoot);
                    if (output) {
                            break;
                    }
            }

            return output;
    }
    
    public static InputStream getFileBytes(String fileURI) 
            throws IOException{
        FileConnection fCon = (FileConnection)Connector.open(fileURI,
            Connector.READ);
        InputStream is = fCon.openInputStream();
        return is;
    }
    
    public static String getFileContents(String fileURI) throws Exception{
        //load from the file

        FileConnection fCon = (FileConnection)Connector.open(fileURI,
            Connector.READ);
        InputStream is = null;
        String str = null;
        if(fCon.exists()) 
            {
                int size = (int)fCon.fileSize();
                is= fCon.openInputStream();
                byte bytes[] = new byte[size];
                is.read(bytes, 0, size);
                str = new String(bytes, 0, size);
                is.close();
                return str;
            }
        return null;
    }
    
    /**
     * Join two paths - make sure there is just one FILE_SEP character 
     * between them
     */
    public static String joinPath(String path1, String path2) {
        if(path1.charAt(path1.length()-1) != FILE_SEP) {
            path1 += FILE_SEP;
        }
        
        if(path2.length() > 0 && path2.charAt(0) == FILE_SEP) {
            path2 = path2.substring(1);
        }
        
        return path1 + path2;
    }
    
    public static String getBaseName(String url){
        int pos = url.lastIndexOf('/');
        return url.substring(pos + 1);
    }
    
    public static long getLastModified(String fileURI){
        try{
            FileConnection fCon = (FileConnection)Connector.open(fileURI,
                Connector.READ);
            long lastModified = fCon.lastModified();
            fCon.close();
            return lastModified;
        }catch(Exception e){}
        return -1;
    }
    
    public static boolean writeStringToFile(String string, String fileURI){
        try{
            FileConnection fileCon = (FileConnection) Connector.open(fileURI, 
                    Connector.READ_WRITE);
            
            if (!fileCon.exists()){
                fileCon.create();
            }
            if (!fileCon.canWrite() && string != null){
                return false;
            }
            
            OutputStream outputStream = fileCon.openOutputStream();
            
            byte[] stringBytes = string.getBytes();
            outputStream.write(stringBytes);
            
            /*
            PrintStream printStream = new PrintStream(outputStream);
            printStream.print(string);
            printStream.close();
            */
            outputStream.close();
            fileCon.close();
            return true;
            
        }catch(Exception e){}
        return false;
    }
    
    public static long getFileSize(String fileURI) throws IOException{
        FileConnection fCon = (FileConnection) Connector.open(fileURI, 
                Connector.READ);
        long size = fCon.fileSize();
        fCon.close();
        return size;
        
    }
    

}