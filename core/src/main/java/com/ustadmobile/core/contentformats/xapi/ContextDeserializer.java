package com.ustadmobile.core.contentformats.xapi;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ContextDeserializer implements JsonDeserializer<ContextActivity> {

    private final Type listType = new TypeToken<ArrayList<XObject>>() {
    }.getType();

    @Override
    public ContextActivity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        ContextActivity contextActivity = new ContextActivity();
        JsonObject jsonObject = json.getAsJsonObject();
        List<XObject> parentList = checkIfArrayOrObject(jsonObject, "parent", context);
        contextActivity.setParent(parentList);

        List<XObject> groupingList = checkIfArrayOrObject(jsonObject, "grouping", context);
        contextActivity.setGrouping(groupingList);

        List<XObject> categoryList = checkIfArrayOrObject(jsonObject, "category", context);
        contextActivity.setCategory(categoryList);

        List<XObject> otherList = checkIfArrayOrObject(jsonObject, "other", context);
        contextActivity.setOther(otherList);

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
                } else {
                    return context.deserialize(elem, listType);
                }
            }
        }
        return null;
    }
}
