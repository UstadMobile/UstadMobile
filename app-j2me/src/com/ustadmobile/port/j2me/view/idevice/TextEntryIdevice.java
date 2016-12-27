/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.view.idevice;

import com.sun.lwuit.TextField;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.html.HTMLElement;
import com.ustadmobile.core.controller.ContainerController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMTinCanUtil;
import java.util.Vector;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 *
 * @author mike
 */
public class TextEntryIdevice extends IdeviceJ2ME{
    
    private HTMLElement inputEl;
    
    public HTMLElement getInputElement() {
        if(inputEl == null) {
            Vector inputElVector = ideviceEl.getDescendantsByTagId(HTMLElement.TAG_INPUT);
            if(inputElVector == null || inputElVector.size() == 0) {
                inputElVector = ideviceEl.getDescendantsByTagId(HTMLElement.TAG_TEXTAREA);
            }
            
            if(inputElVector != null && inputElVector.size() >0) {
                inputEl = (HTMLElement)inputElVector.elementAt(0);
            }
        }
        
        return inputEl;
    }
    
    public void setValue(String value) {
        switch(getInputElement().getTagId()) {
            case HTMLElement.TAG_INPUT:
                inputEl.setAttributeById(HTMLElement.ATTR_VALUE, 
                    value);
                break;
            case HTMLElement.TAG_TEXTAREA:
                int numChildren = inputEl.getNumChildren();
                for(int j = numChildren-1; j >= 0; j--) {
                    inputEl.removeChildAt(j);
                };
                inputEl.addChild(new HTMLElement(value, true));
                break;
            }
    }
    
    public boolean enhance() {
        boolean modified = false;
        getInputElement(); //make sure we have a reference to the input element itself
        String ideviceId = getId();
        String value = null;
        
        if(state != null && state.has(ideviceId)) {
            value = state.optString(ideviceId, null);
            if(value != null) {
                setValue(value);
                modified = true;
            }
        }
        
        return modified;
    }

    public void dataChanged(int i, int i1, HTMLComponent htmlc, TextField tf, HTMLElement htmle) {
        try {
            state.put(getId(), tf.getText());
            ideviceEl.setAttribute("data-dirty", "true");
        }catch(JSONException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 195, null, e);
        }
    }
    
    public void beforeUnload(HTMLComponent htmlC){
        if(ideviceEl.getAttribute("data-dirty") != null && state != null) {
            JSONObject stmt = new JSONObject();
            String responseVal = state.optString(getId(), null);
            try {
                stmt.put("object", UMTinCanUtil.makeActivityObjectById(
                    pageTinCanId + '/' + getId(false)));
                stmt.put("actor", UMTinCanUtil.makeActorFromActiveUser(
                    context));
                stmt.put("verb", UMTinCanUtil.makeVerbObject(
                    "http://adlnet.gov/expapi/verbs/answered", "en-US", "answered"));
                JSONObject resultObj = new JSONObject();
                resultObj.put("response", responseVal);
                stmt.put("result", resultObj);
                if(registrationUUID != null) {
                    context = ContainerController.makeTinCanContext(registrationUUID);
                }else {
                    context = new JSONObject();
                }
                stmt.put("context", context);

                UstadMobileSystemImpl.getInstance().queueTinCanStatement(stmt, 
                    this.context);
                ideviceEl.removeAttribute("data-dirty");
            }catch(JSONException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 195, getId(), e);
            }
        }
    }
    
}
