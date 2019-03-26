package com.ustadmobile.core.contentformats.xapi;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ContextDeserializer implements JsonDeserializer<ContextActivity> {

    @Override
    public ContextActivity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        ContextActivity contextActivity = context.deserialize(json, ContextActivity.class);
        JsonObject jsonObject = json.getAsJsonObject();
        if (contextActivity.getParent() == null) {
            List<XObject> list = checkIfArrayOrObject(jsonObject, "parent", context);
            contextActivity.setParent(list);
        }

        if (contextActivity.getParent() == null) {
            List<XObject> list = checkIfArrayOrObject(jsonObject, "grouping", context);
            contextActivity.setGrouping(list);
        }

        if (contextActivity.getParent() == null) {
            List<XObject> list = checkIfArrayOrObject(jsonObject, "category", context);
            contextActivity.setCategory(list);
        }

        if (contextActivity.getParent() == null) {
            List<XObject> list = checkIfArrayOrObject(jsonObject, "other", context);
            contextActivity.setOther(list);
        }

        return contextActivity;
    }


    public List<XObject> checkIfArrayOrObject(JsonObject json, String objectName, JsonDeserializationContext context) {

        if (json.has(objectName)) {
            JsonElement elem = json.get(objectName);
            if (elem != null && !elem.isJsonNull()) {
                List<XObject> objects = new ArrayList<>();
                if (elem.isJsonObject()) {
                    XObject object = context.deserialize(elem, XObject.class);
                    objects.add(object);
                    return objects;
                }
            }
        }
        return null;
    }
}
