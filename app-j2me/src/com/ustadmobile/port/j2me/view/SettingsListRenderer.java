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

import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.list.ListCellRenderer;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.plaf.UIManager;
import com.ustadmobile.core.model.UserSettingItem;

/**
 *
 * @author mike
 */
public class SettingsListRenderer extends Container implements ListCellRenderer{

    private Label settingName;
    
    private Label settingValue;
        
    public SettingsListRenderer() {
        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        settingName = new Label();
        settingName.getStyle().setBgTransparency(0);
        addComponent(settingName);
        settingValue = new Label();
        settingValue.getStyle().setBgTransparency(0);
        addComponent(settingValue);
        
        setUnselectedStyle(UIManager.getInstance().getComponentStyle("ListItem"));
        setSelectedStyle(UIManager.getInstance().getComponentSelectedStyle("ListItem"));
    }
    
    public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {
        UserSettingItem item = (UserSettingItem)value;
        
        settingName.setText(item.settingName);
        settingValue.setText(item.settingValue);
        if(isSelected) {
            setFocus(true);
        }else {
            setFocus(false);
        }
        
        return this;
    }

    public Component getListFocusComponent(List list) {
        return null;
    }
    
}
