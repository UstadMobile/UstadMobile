package com.ustadmobile.core.contentformats.xapi;

import java.util.Map;

public class Result {

    public boolean completion;

    public boolean success;

    public Score score;

    public String duration;

    public String response;

    private class Score {

        public long scaled;

        public long raw;

        public long min;

        public long max;

    }

    private Map<String, Long> extensions;
}
