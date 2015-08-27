/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.app.forms;

import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextField;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.BrowserComponent;
import com.sun.lwuit.html.DocumentInfo;
import com.sun.lwuit.html.DocumentRequestHandler;
import com.sun.lwuit.*;
import com.sun.lwuit.browser.HttpRequestHandler;
import com.sun.lwuit.io.NetworkManager;
import com.sun.lwuit.io.Storage;
import com.sun.lwuit.layouts.FlowLayout;
import com.ustadmobile.app.FileUtils;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import java.io.InputStream;

        
/**
 *
 * @author varuna
 */
public class HTMLForm {
    
    private static Form f;
    
    private static Form form;
    
    public HTMLForm(Form d){
        f = d;
    }
    public HTMLForm(){
        
    }
    public static Form loadTestForm(){        
        try{
            form = new Form("Hello, LWUIT HTML!");
            HttpRequestHandler  handler = new HttpRequestHandler();
            HTMLComponent htmlC = new HTMLComponent(handler);
            //htmlComp.setPreferredSize(new Dimension(300,300));

            
            String htmlString="<!DOCTYPE html>\n" +
            "<html>\n" +
            "<body>\n" +
            "\n" +
            "<h1 style=\"color:blue\">This is a Blue Heading</h1>\n" +
            "\n" +
            "</body>\n" +
            "</html>";
        
            //htmlComp.setHTML(htmlString, null, null, true);
            
            String url = "http://m.gutenberg.org/";
            url = "http://www.wapnext.com/";
            //htmlC.setPage(url);

            String coursePath = "logo_course/EPUB/comb.xhtml";
            coursePath = "MegaTest/EPUB/free_text_image.xhtml";
            String contentDir = 
                    UstadMobileSystemImpl.getInstance().getSharedContentDir();
            String htmlFile = FileUtils.joinPath(
                    contentDir, coursePath);
            
            String srcPrefix = FileUtils.getFolderPath(htmlFile);
            
            htmlString = FileUtils.getFileContents(htmlFile);
            
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

    public InputStream resourceRequested(DocumentInfo docInfo) {
        return null;
    }
    
}
