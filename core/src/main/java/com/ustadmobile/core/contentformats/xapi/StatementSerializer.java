package com.ustadmobile.core.contentformats.xapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class StatementSerializer implements JsonSerializer<Statement> {


    @Override
    public JsonElement serialize(Statement src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("actor", context.serialize(src.getActor(), Actor.class));
        jsonObject.add("verb", context.serialize(src.getVerb(), Verb.class));
        jsonObject.add("result", context.serialize(src.getResult(), Result.class));
        jsonObject.add("context", context.serialize(src.getContext(), XContext.class));
        jsonObject.addProperty("timestamp", src.getTimestamp());
        jsonObject.addProperty("stored", src.getStored());
        jsonObject.add("authority", context.serialize(src.getAuthority(), Actor.class));
        jsonObject.addProperty("version", src.getVersion());
        jsonObject.addProperty("id", src.getId());
        jsonObject.add("attachments", context.serialize(src.getAttachments(), StatementDeserializer.listType));

        if (src.getSubStatement() != null) {
            jsonObject.add("object", context.serialize(src.getSubStatement(), Statement.class));
        } else {
            jsonObject.add("object", context.serialize(src.getObject(), XObject.class));
        }

        return jsonObject;
    }
}
