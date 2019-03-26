package com.ustadmobile.core.contentformats.xapi;

import com.google.gson.Gson;
import com.google.gson.annotations.JsonAdapter;

import java.util.List;

@JsonAdapter(StatementDeserializer.class)
public class Statement {

    private Actor actor;

    private Verb verb;

    private XObject object;

    private Statement subStatement;

    private Result result;

    private XContext context;

    private String timestamp;

    private String stored;

    private Actor authority;

    private String version;

    private String id;

    private List<Attachment> attachments;

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public Verb getVerb() {
        return verb;
    }

    public void setVerb(Verb verb) {
        this.verb = verb;
    }

    public XObject getObject() {
        return object;
    }

    public void setObject(XObject object) {
        this.object = object;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public XContext getContext() {
        return context;
    }

    public void setContext(XContext context) {
        this.context = context;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStored() {
        return stored;
    }

    public void setStored(String stored) {
        this.stored = stored;
    }

    public Actor getAuthority() {
        return authority;
    }

    public void setAuthority(Actor authority) {
        this.authority = authority;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public Statement getSubStatement() {
        return subStatement;
    }

    public void setSubStatement(Statement subStatement) {
        this.subStatement = subStatement;
    }

    public static Statement loadObject(String json) {

        return new Gson().fromJson(json, Statement.class);
    }
}
