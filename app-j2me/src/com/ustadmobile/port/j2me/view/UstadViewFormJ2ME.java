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
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.plaf.UIManager;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * Is the basis for all Views on J2ME and handles most of the base functionality
 * including maintaining the back button, context, and UI direction
 * 
 * @author mike
 */
public class UstadViewFormJ2ME extends Form {
    
    /**
     * The arguments used to create this view
     */
    private Hashtable args;
    
    private Object context;
    
    private int umViewDirection;
    
    protected Command backCommand;
    
    public static final int CMD_BACK_ID = 1000;
    
    private boolean backCommandEnabled = true;
    
    private UstadViewContainerJ2ME activeContainer;
    
    private String[] menuCommandLabels;
    
    private int[] menuCommandIds;

    public UstadViewFormJ2ME(Hashtable args, Object context, boolean backCommandEnabled) {
        this.args = args;
        this.context = context;
        umViewDirection = UIManager.getInstance().getLookAndFeel().isRTL() ? UstadMobileConstants.DIR_RTL : UstadMobileConstants.DIR_LTR;
        this.backCommandEnabled = backCommandEnabled;
        backCommand = new Command(
                UstadMobileSystemImpl.getInstance().getString(MessageID.back, context), 
                CMD_BACK_ID);
        addCommandListener(new UstadFormCommandListener(context));
        if(this.backCommandEnabled) {
            addBackCommand(1);
        }
    }
    
    public UstadViewFormJ2ME(Hashtable args, Object context) {
        this(args, context, true);
    }
    
    
    public void setAppMenuCommands(String[] labels, int[] ids) {
        this.menuCommandLabels = labels;
        this.menuCommandIds = ids;
    }

    public boolean isIsBackCommandEnabled() {
        return backCommandEnabled;
    }

    public void setIsBackCommandEnabled(boolean backCommandEnabled) {
        this.backCommandEnabled = backCommandEnabled;
    }
    
    /**
     * Adds the back command to the form 
     * @param minHistoryEntries Minimum number of history entries available.  If this is during form construction - there
     * only needs to be one entry available.  After the constructor the current form is an element in the history stack
     * and there should be at least 2 entries for the user to be able to go back to the previous form
     */
    protected void addBackCommand(int minHistoryEntries) {
        if(isBackCommandAvailable(minHistoryEntries)) {
            addCommand(backCommand);
        }
    }
    
    protected boolean isBackCommandAvailable(int minHistoryEntries) {
        return backCommandEnabled && UstadMobileSystemImplJ2ME.getInstanceJ2ME().getViewHistorySize() >= minHistoryEntries;
    }
    
    protected void addBackCommand() {
        addBackCommand(2);
    }
    
    protected Hashtable getArgs() {
        return args;
    }
    
    public Object getContext() {
        return context;
    }
    
    public void setDirection(int direction) {
        if(umViewDirection != direction) {
            boolean isRTL = direction == UstadMobileConstants.DIR_RTL;
            //don't use applyRTL etc. here - it will confuse the soft buttons
            UIManager.getInstance().getLookAndFeel().setRTL(isRTL);
            umViewDirection = direction;
        }
    }
    
    public int getDirection() {
        return umViewDirection;
    }
    
    public class UstadFormCommandListener implements ActionListener{

        final private Object context;
        
        public UstadFormCommandListener(Object context) {
            this.context = context;
        }
        
        public void actionPerformed(ActionEvent evt) {
            if(evt.getCommand() != null && evt.getCommand().getId() == UstadViewFormJ2ME.CMD_BACK_ID) {
                UstadMobileSystemImplJ2ME impl = UstadMobileSystemImplJ2ME.getInstanceJ2ME();
                impl.goBack(context);
            }else if(evt.getCommand() != null){
                UstadBaseController.handleClickAppMenuItem(evt.getCommand().getId(),
                    context);
            }
        }
        
    }
    
    /**
     * Tells the system implementation if this form can go back internally... 
     * 
     * @return true if this form can go back, false otherwise
     */
    public boolean canGoBack() {
        return false;
    }
    
    /**
     * Tells the form to go back internally - by default does nothing
     */
    public void goBack() {
        
    }
    
    /**
     * Handle shut down as required - the user is about to leave...
     */
    public void onDestroy() {
        if(activeContainer != null) {
            activeContainer.onDestroy();
        }
        
    }
    
    public void onCreateMenuCommands(Vector cmdVector) {
        if(menuCommandIds != null) {
            for(int i = 0; i < menuCommandIds.length; i++) {
                cmdVector.addElement(new Command(menuCommandLabels[i], 
                    menuCommandIds[i]));
            }
        }
    }
    
    public void invalidateMenuCommands() {
        removeAllCommands();
        Vector cmdVector = new Vector();
        
        if(isBackCommandAvailable(2)) {
            cmdVector.addElement(backCommand);
        }
        
        onCreateMenuCommands(cmdVector);
        if(this.activeContainer != null) {
            this.activeContainer.onCreateMenuCommands(cmdVector);
        }
        
        for(int i = 0;i < cmdVector.size(); i++) {
            addCommand((Command)cmdVector.elementAt(i));
        }
    }
    
    public void setActiveUstadViewContainer(UstadViewContainerJ2ME container) {        
        if(container != this.activeContainer) {
            if(this.activeContainer != null) {
                removeCommandListener(this.activeContainer);
            }
            
            this.activeContainer = container;
            invalidateMenuCommands();
            addCommandListener(container);
            invalidateTitle();
        }
    }
    
    /**
     * Used by containers to tell us to look again at the title
     */
    public void invalidateTitle() {
        if(this.activeContainer != null) {
            String containerTitle = activeContainer.getTitle();
            if(containerTitle != null) {
                setTitle(containerTitle);
            }
        }
    }
    
    public void setUIStrings() {
        //placeholder
    }
    
    public void runOnUiThread(Runnable r) {
        Display.getInstance().callSerially(r);
    }
    
}
