package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AuditLogSelectionView;
import com.ustadmobile.core.view.GroupListView;
import com.ustadmobile.core.view.HolidayCalendarListView;
import com.ustadmobile.core.view.LocationListView;
import com.ustadmobile.core.view.RoleAssignmentListView;
import com.ustadmobile.core.view.RoleListView;
import com.ustadmobile.core.view.SELQuestionSetsView;
import com.ustadmobile.core.view.SettingsView;

import java.util.Hashtable;

public class SettingsPresenter extends UstadBaseController<SettingsView> {


    public SettingsPresenter(Object context, Hashtable arguments, SettingsView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
    }

    public void goToSELQuestionSets(){
        Hashtable args = new Hashtable();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.go(SELQuestionSetsView.VIEW_NAME, args, context);
    }

    public void goToHolidayCalendarList() {
        Hashtable args = new Hashtable();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.go(HolidayCalendarListView.VIEW_NAME, args, context);
    }


    public void goToRolesList() {
        Hashtable args = new Hashtable();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.go(RoleListView.VIEW_NAME, args, context);
    }

    public void goToGroupsList() {
        Hashtable args = new Hashtable();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.go(GroupListView.VIEW_NAME, args, context);
    }

    public void goToRolesAssignmentList() {
        Hashtable args = new Hashtable();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.go(RoleAssignmentListView.VIEW_NAME, args, context);
    }

    public void goToLocationsList() {
        Hashtable args = new Hashtable();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.go(LocationListView.VIEW_NAME, args, context);
    }

    public void goToAuditLogSelection() {
        Hashtable args = new Hashtable();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.go(AuditLogSelectionView.VIEW_NAME, args, context);
    }
}
