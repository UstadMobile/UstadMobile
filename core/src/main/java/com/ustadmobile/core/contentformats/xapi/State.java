package com.ustadmobile.core.contentformats.xapi;

import java.util.HashMap;

public class State {

    private Actor agent;

    private String activityId;

    private String stateId;

    private String registration;

    private HashMap<String, Object> content;

    public State(String stateId, Actor agent, String activityId, HashMap<String, Object> contentMap, String registration) {
        this.agent = agent;
        this.stateId = stateId;
        this.activityId = activityId;
        this.content = contentMap;
        this.registration = registration;
    }

    public Actor getAgent() {
        return agent;
    }

    public void setAgent(Actor agent) {
        this.agent = agent;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public HashMap<String, Object> getContent() {
        return content;
    }

    public void setContent(HashMap<String, Object> content) {
        this.content = content;
    }
}
