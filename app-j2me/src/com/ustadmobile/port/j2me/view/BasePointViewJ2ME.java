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
import com.sun.lwuit.Component;
import com.sun.lwuit.RadioButton;
import com.sun.lwuit.Tabs;
import com.sun.lwuit.events.FocusListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.controller.BasePointController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.BasePointMenuItem;
import com.ustadmobile.core.view.BasePointView;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author mike
 */
public class BasePointViewJ2ME extends UstadViewFormJ2ME implements BasePointView, FocusListener {

    //private Tabs tabs;
    private Tabs tabs;
    
    private CatalogOPDSContainer[] opdsContainers;
    
    private BasePointController basePointController;
    
    private int[] tabTitles =  new int[]{MessageID.my_resources, 
        MessageID.browse_feeds};
        
    /**
     * At this point J2ME does not support the class list tab: is one tab - just my resources
     */
    public static final int NUM_TABS = 1;
    
    private static final int OFFSET_BASEPOINT_MENU_CMDS = 2000;
    
    
    public BasePointViewJ2ME(Hashtable args, Object context, boolean backCommandEnabled) {
        super(args, context, backCommandEnabled);
        setLayout(new BorderLayout());
        basePointController = BasePointController.makeControllerForView(this, args, 
                null);
    }
    
    
    public void initComponent() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        if(tabs == null) {
            tabs = new Tabs(Component.TOP);
            tabs.addTabsFocusListener(this);
            tabs.setChangeTabOnFocus(true);
            opdsContainers = new CatalogOPDSContainer[NUM_TABS];
            Hashtable opdsArgs;
            for(int i = 0; i < NUM_TABS; i++) {
                opdsArgs = basePointController.getCatalogOPDSArguments(i);
                opdsContainers[i] = new CatalogOPDSContainer(opdsArgs, 
                    getContext(), this);
                tabs.addTab(impl.getString(tabTitles[i], getContext()), opdsContainers[i]);
            }
            
            setActiveUstadViewContainer(opdsContainers[0]);
            setLayout(new BorderLayout());
            addComponent(BorderLayout.CENTER, tabs);
        }
    }

    /**
     * Add the menu options as per the build config
     * 
     * TODO: Handle the order of this menu in the Form system
     * 
     * @param cmdVector 
     */
    public void onCreateMenuCommands(Vector cmdVector) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Command cmd;
        
        BasePointMenuItem[] menu = impl.getActiveUser(getContext()) != null ?
                CoreBuildConfig.BASEPOINT_MENU_AUTHENTICATED 
                : CoreBuildConfig.BASEPOINT_MENU_GUEST;
        for(int i = 0; i < menu.length; i++) {
            cmd = new Command(impl.getString(
                menu[i].getTitleStringId(), getContext()), 
                OFFSET_BASEPOINT_MENU_CMDS+ i);
            cmdVector.addElement(cmd);
        }
    }
    
    
    
    private int getTabSelectedByTitle(String title) {
        for(int i = 0; i < tabs.getTabCount(); i++) {
            if(tabs.getTabTitle(i).equals(title)) {
                return i;
            }
        }
        
        return -1;
    }
    

    public void focusGained(Component cmpnt) {
        RadioButton btn = (RadioButton)cmpnt;
        int selectedIndex = getTabSelectedByTitle(btn.getText());
        setActiveUstadViewContainer(opdsContainers[selectedIndex]);
    }

    public void focusLost(Component cmpnt) {
    }

    public void setClassListVisible(boolean visible) {
        //this is not supported on J2ME
    }
    
    public void refreshCatalog(int column) {
        opdsContainers[column].loadCatalog();
    }
    
    
}
