package com.ustadmobile.core.contentformats.xapi;

import java.util.List;
import java.util.Map;

public class Definition {

    public Map<String, String> name;

    public Map<String, String> description;

    public String type;

    public Map<String, String> extensions;

    public String moreInfo;

    public String interactionType;

    private List<String> correctResponsePattern;

    public List<Interaction> choices;

    public List<Interaction> scale;

    public List<Interaction> source;

    public List<Interaction> target;

    public List<Interaction> steps;

    private class Interaction {

        private String id;

        private Map<String, String> description;

    }


}
