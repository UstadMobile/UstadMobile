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
package com.ustadmobile.port.j2me.view;

import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.plaf.UIManager;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.core.view.UstadView;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author mike
 */
public class UstadViewContainerJ2ME extends Container implements UstadView, ActionListener {

    private Hashtable args;
    
    private Object context;
    
    private int umViewDirection;
    
    private UstadViewFormJ2ME ustadForm;
    
    Vector appMenuCommands;
    
    public UstadViewContainerJ2ME(Hashtable args, Object context, UstadViewFormJ2ME ustadForm) {
        this.args = args;
        this.context = context;
        //TODO: Centralize this
        this.umViewDirection = UIManager.getInstance().getLookAndFeel().isRTL() ? UstadMobileConstants.DIR_RTL : UstadMobileConstants.DIR_LTR;
        this.ustadForm = ustadForm;
        appMenuCommands = new Vector();
    }
    
    public void setAppMenuCommands(String[] labels, int[] ids) {
        appMenuCommands.removeAllElements();
        
        for(int i = 0; i < labels.length; i++) {
            appMenuCommands.addElement(new Command(labels[i], ids[i]));
        }
        
        ustadForm.invalidateMenuCommands();
    }
    
    public void onCreateMenuCommands(Vector cmdVector) {
        for(int i = 0; i < appMenuCommands.size(); i++) {
            cmdVector.addElement(appMenuCommands.elementAt(i));
        }
    }
    
    public Object getContext() {
        return context;
    }

    public int getDirection() {
        return umViewDirection;
    }

    public void setDirection(int direction) {
         if(umViewDirection != direction) {
            boolean isRTL = direction == UstadMobileConstants.DIR_RTL;
            //don't use applyRTL etc. here - it will confuse the soft buttons
            UIManager.getInstance().getLookAndFeel().setRTL(isRTL);
            umViewDirection = direction;
        }
    }
    
    public UstadViewFormJ2ME getUstadForm() {
        return ustadForm;
    }
    
    protected Hashtable getArgs() {
        return this.args;
    }
    
    public void onDestroy() {      
        
    }
    
    

    /**
     * Will receive action performed events when this is the active containe
     * 
     * by default does nothing
     * 
     * @param ae 
     */
    public void actionPerformed(ActionEvent ae) {
        
    }
    
    public String getTitle() {
        return null;
    }
    
    public void setUIStrings() {
        //by default - do nothing
    }

    public void runOnUiThread(Runnable r) {
        Display.getInstance().callSerially(r);
    }
    
    
    
}