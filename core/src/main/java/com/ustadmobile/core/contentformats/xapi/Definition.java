package com.ustadmobile.core.contentformats.xapi;

import java.util.List;
import java.util.Map;

public class Definition {

    private Map<String, String> name;

    private Map<String, String> description;

    private String type;

    private Map<String, String> extensions;

    private String moreInfo;

    private String interactionType;

    private List<String> correctResponsePattern;

    private List<Interaction> choices;

    private List<Interaction> scale;

    private List<Interaction> source;

    private List<Interaction> target;

    private List<Interaction> steps;

    private class Interaction {

        private String id;

        private Map<String, String> description;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Map<String, String> getDescription() {
            return description;
        }

        public void setDescription(Map<String, String> description) {
            this.description = description;
        }
    }

    public Map<String, String> getName() {
        return name;
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, String> extensions) {
        this.extensions = extensions;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    public String getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(String interactionType) {
        this.interactionType = interactionType;
    }

    public List<String> getCorrectResponsePattern() {
        return correctResponsePattern;
    }

    public void setCorrectResponsePattern(List<String> correctResponsePattern) {
        this.correctResponsePattern = correctResponsePattern;
    }

    public List<Interaction> getChoices() {
        return choices;
    }

    public void setChoices(List<Interaction> choices) {
        this.choices = choices;
    }

    public List<Interaction> getScale() {
        return scale;
    }

    public void setScale(List<Interaction> scale) {
        this.scale = scale;
    }

    public List<Interaction> getSource() {
        return source;
    }

    public void setSource(List<Interaction> source) {
        this.source = source;
    }

    public List<Interaction> getTarget() {
        return target;
    }

    public void setTarget(List<Interaction> target) {
        this.target = target;
    }

    public List<Interaction> getSteps() {
        return steps;
    }

    public void setSteps(List<Interaction> steps) {
        this.steps = steps;
    }
}
