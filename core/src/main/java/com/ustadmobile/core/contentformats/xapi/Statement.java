package com.ustadmobile.core.contentformats.xapi;

import java.util.List;

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

    private String objectType;

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

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Statement statement = (Statement) o;

        if (actor != null ? !actor.equals(statement.actor) : statement.actor != null) return false;
        if (verb != null ? !verb.equals(statement.verb) : statement.verb != null) return false;
        if (object != null ? !object.equals(statement.object) : statement.object != null)
            return false;
        if (subStatement != null ? !subStatement.equals(statement.subStatement) : statement.subStatement != null)
            return false;
        if (result != null ? !result.equals(statement.result) : statement.result != null)
            return false;
        if (context != null ? !context.equals(statement.context) : statement.context != null)
            return false;
        if (authority != null ? !authority.equals(statement.authority) : statement.authority != null)
            return false;
        return objectType != null ? objectType.equals(statement.objectType) : statement.objectType == null;
    }

    @Override
    public int hashCode() {
        int result1 = actor != null ? actor.hashCode() : 0;
        result1 = 31 * result1 + (verb != null ? verb.hashCode() : 0);
        result1 = 31 * result1 + (object != null ? object.hashCode() : 0);
        result1 = 31 * result1 + (subStatement != null ? subStatement.hashCode() : 0);
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        result1 = 31 * result1 + (context != null ? context.hashCode() : 0);
        result1 = 31 * result1 + (authority != null ? authority.hashCode() : 0);
        result1 = 31 * result1 + (objectType != null ? objectType.hashCode() : 0);
        return result1;
    }
}
