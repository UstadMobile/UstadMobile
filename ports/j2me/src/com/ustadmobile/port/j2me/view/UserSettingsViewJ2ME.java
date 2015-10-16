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

import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.List;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.list.DefaultListModel;
import com.sun.lwuit.list.ListModel;
import com.ustadmobile.core.controller.UserSettingsController;
import com.ustadmobile.core.model.UserSettingItem;
import com.ustadmobile.core.view.UserSettingsView;
import java.util.Hashtable;

/**
 *
 * @author mike
 */
public class UserSettingsViewJ2ME extends UstadViewFormJ2ME implements UserSettingsView, ActionListener {
    
    private List settingsList; 
    
    private DefaultListModel settingsListModel;
        
    private UserSettingsController controller;
       
    private List languageList;
    
    private DefaultListModel languageListModel;
    
    private Component currentComp;

    public UserSettingsViewJ2ME(Hashtable args, Object context) {
        super(args, context);
        
        settingsListModel = new DefaultListModel();
        settingsList = new List(settingsListModel);
        settingsList.setRenderer(new SettingsListRenderer());
        settingsList.addActionListener(this);
        
        setLayout(new BorderLayout());
        addComponent(BorderLayout.CENTER, settingsList);
        currentComp = settingsList;
        
        languageListModel = new DefaultListModel();
        languageList = new List(languageListModel);
        languageList.addActionListener(this);
        
        controller = UserSettingsController.makeControllerForView(this);
    }

    public void setSettingsTitle(String title) {
        setTitle(title);
    }

    public void setSettingsList(UserSettingItem[] items) {
        settingsListModel.removeAll();
        for(int i = 0; i < items.length; i++) {
            settingsListModel.addItem(items[i]);
        }
        
    }

    public void setLanguageList(String[] languages) {
        languageListModel.removeAll();
        for(int i = 0; i < languages.length; i++) {
            languageListModel.addItem(languages[i]);
        }
    }

    private void setCurrentComp(Component newComp) {
        if(currentComp != newComp) {
            removeComponent(currentComp);
            addComponent(BorderLayout.CENTER, newComp);
            currentComp = newComp;
        }
    }
    
    public void showSettingsList() {
        setCurrentComp(settingsList);
    }

    public void showLanguageList() {
        setCurrentComp(languageList);
    }

    public void actionPerformed(ActionEvent evt) {
        if(evt.getComponent() == settingsList) {
            controller.handleClickSetting(settingsList.getSelectedIndex());
        }else if(evt.getComponent() == languageList) {
            controller.handleClickLanguage(languageList.getSelectedIndex());
        }
    }

    
}
