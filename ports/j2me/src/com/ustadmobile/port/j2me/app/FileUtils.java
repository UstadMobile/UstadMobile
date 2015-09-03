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

package com.ustadmobile.port.j2me.app;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
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
    public static final String EPUB_CONTAINER = "META-INF/container.xml";

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
        if(bestRoot.path != null || bestRoot.path != ""){
            if(bestRoot.path.startsWith(FILE_PREFIX + FILE_SEP)){
                try {
                    HTTPUtils.makeHTTPRequest(
                    "http://umcloud1.ustadmobile.com/NullSDCard/"+
                            "",
                            null, null);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            
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
    
    public static boolean checkDir(String dirName) throws IOException{
            dirName = dirName.trim();
            if (!dirName.endsWith("/")){
                dirName += "/";
            }
            boolean exists = false;
            FileConnection fc = null;
            fc = (FileConnection) Connector.open(dirName, 
                Connector.READ_WRITE);
            if (fc.exists()){
                exists = true;
            }
            if (fc != null){
                fc.close();
            }
            
            return exists;
          
    }
    
    public static boolean checkFile(String fileURI) throws IOException{
        fileURI = fileURI.trim();
        FileConnection fc = (FileConnection)Connector.open(fileURI,
                Connector.READ);
        if(fc.exists()){
            fc.close();
            return true;
        }
        
        return false;
    }

    
    public static boolean deleteRecursively(String path, boolean recursive) 
            throws IOException {
      FileConnection file = (FileConnection) Connector.open(path, 
              Connector.READ_WRITE);
      if (!file.exists()) {
          return false;
      }

      if (!recursive || !file.isDirectory()){
          file.delete();
          if (!file.exists()){
              file.close();
              return true;
          }
          return false;
      }

      
      Enumeration listEnu = file.list();
      String[] list;
      list = enumerationToStringArray(listEnu);
      
      for (int i = 0; i < list.length; i++) {
          if (!deleteRecursively(FileUtils.joinPath(path, list[i]), true)){
              return false;
          }
      }
      file.delete();
      if (!file.exists()){
          file.close();
          return true;
      }else{
          return false;
      }
      
  }

    
    public static boolean removeFileOrDir(String path, int mode, 
            boolean isDir) throws IOException{
        path = path.trim();
        if (isDir && !path.endsWith("/")){
            path += "/";
        }
        FileConnection fc = (FileConnection) Connector.open(path, mode);
        if (fc.exists()){
            if (isDir && !fc.isDirectory()){
                return false;
            }
            fc.delete();
            fc.close();
            return true;
        }
        fc.close();
        
        return false;
    }
    
    
     /*
     * 
     * Other utils 
     * 
     */
    
    public static String[] enumerationToStringArray(Enumeration enu){
        String[] list;
        Vector listVector = new Vector();
        while(enu.hasMoreElements()){
            listVector.addElement(enu.nextElement());
        }
        list = new String[listVector.size()];
        listVector.copyInto(list);
        return list;
    }
    
    
    public static String replaceString( String str, String pattern, String replace ) 
    {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();

        while ( (e = str.indexOf( pattern, s ) ) >= 0 ) 
        {
            result.append(str.substring( s, e ) );
            result.append( replace );
            s = e+pattern.length();
        }
        result.append( str.substring( s ) );
        return result.toString();
    }   
    
    
    public static Vector addTwoVectors(Vector v1, Vector v2){
        Vector v  = null;
        Enumeration e = null;
        e = v2.elements();
        while (e.hasMoreElements()){
            v1.addElement(e.nextElement());
        }
        return v1;
    }
    
    public static Vector hashtableToVector(Hashtable h){
        String[] stringArray = null;
        Vector v = null;
        if (!h.isEmpty()){
            v = new Vector();
            Enumeration e = h.elements();
            while (e.hasMoreElements()){
                v.addElement(e.nextElement());
            }
        }
        return v;
    }
    
    
    /*
     * 
     * end of common utils.
     */
    
    public static Vector listFilesRecursivelyInDirectory(String dirURI,
            String parentDirURI) throws IOException{
        
        dirURI = dirURI.trim();
        parentDirURI = parentDirURI.trim();
        
        if (!dirURI.endsWith("/")){
            dirURI += "/";
        }
        if (!parentDirURI.endsWith("/")){
            parentDirURI += "/";
        }
        FileConnection fc = null;
        Vector dirListVector = new Vector();
        Vector listVector = new Vector();
        
        fc = (FileConnection) Connector.open(dirURI, 
                Connector.READ);
        
        if(fc.isDirectory()){
            String toAdd = null;
            toAdd = replaceString(dirURI, parentDirURI, "");
            Enumeration dirListEnu = fc.list();
            if (fc != null){
                fc.close();
            }            
            while(dirListEnu.hasMoreElements()){
                String entry = dirListEnu.nextElement().toString().trim();
                //If entry is a directory
                if (entry.endsWith("/")){
                    Vector nextVector = new Vector();
                    String subDirURI = FileUtils.joinPath(dirURI, entry);
                    if ( (nextVector = listFilesRecursivelyInDirectory(subDirURI, parentDirURI)) == null){
                        return null;
                    }
                    listVector = addTwoVectors(listVector, nextVector);
                    
                }else{
                    listVector.addElement(toAdd + entry);
                }
                
            }
            
        }
        
        if (fc != null){
            fc.close();
        }
        return listVector;
    }
    
    public static String[] vectorToStringArray(Vector v){
        String[] sa = null;
        if (!v.isEmpty()){
            sa = new String[v.size()];
            v.copyInto(sa);
            return sa;
        }
        return sa;
    }
    
    public static boolean isStringInStringArray(String str, String[] strArray){
        if (str != null && strArray.length > 0 ){
            boolean found = false;
            for (int i = 0; i< strArray.length; i++){
                if (strArray[i].indexOf(str) >= 0){
                    found = true;
                    break;
                }
            }
            return found;
        }
        return false;
    }
    
    public static String[] listFilesInDirectory(String dirURI) throws IOException{
        
        if (!dirURI.endsWith("/")){
            dirURI += "/";
        }
        FileConnection fc = null;
        String dirList[] = null;
        fc = (FileConnection) Connector.open(dirURI, 
                Connector.READ);
        if(fc.isDirectory()){
            Enumeration dirListEnu = fc.list();
            dirList = enumerationToStringArray(dirListEnu);
        }
        if (fc != null){
            fc.close();
        }
        return dirList;
        
    }
    public static boolean renameFileOrDir(String path, String pathTo, int mode, 
            boolean isDir) throws IOException{
        path = path.trim();
        pathTo = pathTo.trim();
        String newName = FileUtils.getBaseName(pathTo);
        if (isDir && !path.endsWith("/")){
            path += "/";
        }
        FileConnection fc = (FileConnection) Connector.open(path, mode);
        if (fc.exists()){
            fc.rename(newName);
            fc.close();
            return true;
        }
        
        return false;
    }

    public static boolean createFileOrDir(String fileName, int mode, 
            boolean isDir) throws IOException {

            boolean created = false;
            FileConnection fc = null;
            fileName = fileName.trim();
            HTTPUtils.httpDebug("CreateFileOrDirFileNameIs");
            HTTPUtils.httpDebug(fileName.substring(10));

            if (isDir && !fileName.endsWith("/")) {
                    fileName += "/";
            }
            
            //fileName = fileName.substring(0, fileName.length()-1);

            try{
                fc = (FileConnection) 
                    Connector.open(fileName, mode);
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
                
            }catch(Exception e){
                e.printStackTrace();
                HTTPUtils.httpDebug("Exception");
                HTTPUtils.httpDebug(e.getMessage());
            }finally{
                if(fc!=null){
                    fc.close();
                }
            }
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
        fCon.close();
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
                fCon.close();
                return str;
            }
        return null;
    }
    
    public static String joinPath(String path1, String path2) {
        return joinPath(path1, path2, false);
    }
    
    /**
     * Join two paths - make sure there is just one FILE_SEP character 
     * between them
     */
    public static String joinPath(String path1, String path2, boolean isDir) {
        if(path1.charAt(path1.length()-1) != FILE_SEP) {
            path1 += FILE_SEP;
        }
        
        if(path2.length() > 0 && path2.charAt(0) == FILE_SEP) {
            path2 = path2.substring(1);
        }
        
        if(path2.charAt(path2.length()-1) == FILE_SEP){
            path2 = path2.substring(0,path2.length());
        }
        
        if (isDir){
            return path1+path2+FILE_SEP;
        }else{
            return path1 + path2;
        }
    }
    
    public static String getFolderPath (String absolutePath){
        int pos = absolutePath.lastIndexOf('/');
        return absolutePath.substring(0, pos) + "/";
        
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
    
    public static boolean writeStringToFile(String string, String fileURI, 
            boolean append){
        try{
            if (!append){
                deleteRecursively(fileURI, false);
            }
            FileConnection fileCon = (FileConnection) Connector.open(fileURI, 
                    Connector.READ_WRITE);
            
            if (!fileCon.exists()){
                fileCon.create();
            }
            if (!fileCon.canWrite() && string != null){
                return false;
            }
            OutputStream outputStream = null;
            if(append){
                outputStream = fileCon.openOutputStream(fileCon.fileSize());
            }else{
                outputStream = fileCon.openDataOutputStream();
            }
            
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