/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ustadmobile.app.forms;

import com.sun.lwuit.Component;
import com.sun.lwuit.Form;
import com.sun.lwuit.browser.HttpRequestHandler;
import com.sun.lwuit.html.DefaultDocumentRequestHandler;
import com.sun.lwuit.html.DocumentInfo;
import com.sun.lwuit.html.DocumentRequestHandler;
import com.sun.lwuit.layouts.FlowLayout;
import com.ustadmobile.app.EpubUtils;
import com.ustadmobile.app.FileUtils;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import gnu.classpath.java.util.zip.ZipInputStream;
import java.io.InputStream;


/**
 *
 * @author varuna
 */
public class HTMLComp {
    
    private static Form form;
    
    public static Form loadTestForm(String epubFile, String pageName){        
        try{
            
            /*
            InputStream zis = EpubUtils.getPageFromEpub(epubFile, pageName);
            form = new Form("Hello, LWUIT HTML!");
                        
            DocumentRequestHandler documentRequestHandler = 
                    new DefaultDocumentRequestHandler();
            
            com.sun.lwuit.html.HTMLComponent htmlC = 
                    new com.sun.lwuit.html.HTMLComponent(documentRequestHandler);
            htmlC.setPage(pageName);
            */
            
            
            HttpRequestHandler  handler = new HttpRequestHandler();
            com.sun.lwuit.html.HTMLComponent htmlC = 
                    new com.sun.lwuit.html.HTMLComponent(handler);

            String coursePath = "logo_course/EPUB/comb.xhtml";
            coursePath = "MegaTest/EPUB/free_text_image.xhtml";
            String contentDir = 
                    UstadMobileSystemImpl.getInstance().getSharedContentDir();
            String htmlFile = FileUtils.joinPath(
                    contentDir, coursePath);
            String srcPrefix = FileUtils.getFolderPath(htmlFile);
            String htmlString = FileUtils.getFileContents(htmlFile);
            
            //replace src paths.
            htmlString = FileUtils.replaceString(htmlString, 
                    "src=\"", "src=\""+srcPrefix);
            htmlC.setHTML(htmlString, null, null, true);
            
            
            FlowLayout flow = new FlowLayout(Component.TOP);
            form.setLayout(flow);
            form.addComponent(htmlC);
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return form;
    }
    
    
}
