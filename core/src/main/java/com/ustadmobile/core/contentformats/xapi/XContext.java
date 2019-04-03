package com.ustadmobile.core.contentformats.xapi;

import java.util.Map;

public class XContext {

    private Actor instructor;

    private String registration;

    private String language;

    private String platform;

    private String revision;

    private Actor team;

    private XObject statement;

    private ContextActivity contextActivities;

    private Map<String, String> extensions;

    public Actor getInstructor() {
        return instructor;
    }

    public void setInstructor(Actor instructor) {
        this.instructor = instructor;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public XObject getStatement() {
        return statement;
    }

    public void setStatement(XObject statement) {
        this.statement = statement;
    }

    public ContextActivity getContextActivities() {
        return contextActivities;
    }

    public void setContextActivities(ContextActivity contextActivities) {
        this.contextActivities = contextActivities;
    }

    public Map<String, String> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, String> extensions) {
        this.extensions = extensions;
    }

    public Actor getTeam() {
        return team;
    }

    public void setTeam(Actor team) {
        this.team = team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XContext xContext = (XContext) o;

        if (instructor != null ? !instructor.equals(xContext.instructor) : xContext.instructor != null)
            return false;
        if (registration != null ? !registration.equals(xContext.registration) : xContext.registration != null)
            return false;
        if (language != null ? !language.equals(xContext.language) : xContext.language != null)
            return false;
        if (platform != null ? !platform.equals(xContext.platform) : xContext.platform != null)
            return false;
        if (revision != null ? !revision.equals(xContext.revision) : xContext.revision != null)
            return false;
        if (team != null ? !team.equals(xContext.team) : xContext.team != null) return false;
        if (statement != null ? !statement.equals(xContext.statement) : xContext.statement != null)
            return false;
        if (contextActivities != null ? !contextActivities.equals(xContext.contextActivities) : xContext.contextActivities != null)
            return false;
        return extensions != null ? extensions.equals(xContext.extensions) : xContext.extensions == null;
    }

    @Override
    public int hashCode() {
        int result = instructor != null ? instructor.hashCode() : 0;
        result = 31 * result + (registration != null ? registration.hashCode() : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (platform != null ? platform.hashCode() : 0);
        result = 31 * result + (revision != null ? revision.hashCode() : 0);
        result = 31 * result + (team != null ? team.hashCode() : 0);
        result = 31 * result + (statement != null ? statement.hashCode() : 0);
        result = 31 * result + (contextActivities != null ? contextActivities.hashCode() : 0);
        result = 31 * result + (extensions != null ? extensions.hashCode() : 0);
        return result;
    }
}
