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
package com.ustadmobile.core.tincan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
/* $endif$ */

/**
 * A very small utility class for wrapping a TinCan Statement which in fact
 * simply a child class of JSONObject
 * 
 * @author mike
 */
public class TinCanStatement extends JSONObject{
    
    private String registrationUUID;
    
    public TinCanStatement(String stmt) throws JSONException {
        super(stmt);
    }
    
    public TinCanStatement(JSONObject obj) throws JSONException {
        super(obj, getNamesArray(obj));
    }
    
    public static String[] getNamesArray(JSONObject obj) {
        String[] arr = null;
        try {
            JSONArray jsonArr = obj.names();
            final int numNames = jsonArr.length();
            arr = new String[numNames];
            for(int i = 0; i < numNames; i++) {
                arr[i] = jsonArr.getString(i);
            }
        }catch(JSONException e) {
            //This should almost never happen : we don't exceed the length of array etc - names are strings
            e.printStackTrace();
        }
        
        return arr;
    }
    
    /**
     * Get the registration UUID associated with this statement (if any)
     * 
     * @return Registration UUID as a string; or null if there is none
     */
    public String getRegistrationUUID() {
        if(registrationUUID != null)
            return registrationUUID;
        
        try {
            if(has("context")) {
                JSONObject ctx = getJSONObject("context");
                if(ctx.has("registration")) {
                    registrationUUID = ctx.getString("registration");
                    return registrationUUID;
                }
            }
        }catch(JSONException e) {
            e.printStackTrace();//can only happen if it has a context object that's not a json object...
        }
        
        return null;
    }
    
}
