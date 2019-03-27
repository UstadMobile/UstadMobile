package com.ustadmobile.core.contentformats.xapi;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class StatementDeserializer implements JsonDeserializer<Statement> {

    public static final Type listType = new TypeToken<ArrayList<Attachment>>() {
    }.getType();


    @Override
    public Statement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        Statement statement = new Statement();
        JsonObject jObject = json.getAsJsonObject();

        statement.setActor(context.deserialize(jObject.get("actor"), Actor.class));
        statement.setVerb(context.deserialize(jObject.get("verb"), Verb.class));
        statement.setResult(context.deserialize(jObject.get("result"), Result.class));
        statement.setContext(context.deserialize(jObject.get("context"), XContext.class));
        statement.setTimestamp(jObject.has("timestamp") ? jObject.get("timestamp").getAsString() : null);
        statement.setStored(jObject.has("stored") ? jObject.get("stored").getAsString() : null);
        statement.setAuthority(context.deserialize(jObject.get("authority"), Actor.class));
        statement.setVersion(jObject.has("version") ? jObject.get("version").getAsString() : null);
        statement.setId(jObject.has("id") ? jObject.get("id").getAsString() : null);
        statement.setAttachments(context.deserialize(jObject.get("attachments"), listType));

        XObject object = context.deserialize(jObject.get("object"), XObject.class);
        if (object.getObjectType().equals("SubStatement")) {
            statement.setSubStatement(context.deserialize(jObject.get("object"), Statement.class));
        } else {
            statement.setObject(object);
        }

        return statement;
    }
}
