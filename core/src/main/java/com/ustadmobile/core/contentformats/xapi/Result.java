package com.ustadmobile.core.contentformats.xapi;

import java.util.Map;

public class Result {

    private boolean completion;

    private boolean success;

    private Score score;

    private String duration;

    private String response;

    public class Score {

        private long scaled;

        private long raw;

        private long min;

        private long max;

        public long getScaled() {
            return scaled;
        }

        public void setScaled(long scaled) {
            this.scaled = scaled;
        }

        public long getRaw() {
            return raw;
        }

        public void setRaw(long raw) {
            this.raw = raw;
        }

        public long getMin() {
            return min;
        }

        public void setMin(long min) {
            this.min = min;
        }

        public long getMax() {
            return max;
        }

        public void setMax(long max) {
            this.max = max;
        }
    }

    private Map<String, Object> extensions;

    public boolean isCompletion() {
        return completion;
    }

    public void setCompletion(boolean completion) {
        this.completion = completion;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Result result = (Result) o;

        if (completion != result.completion) return false;
        if (success != result.success) return false;
        if (score != null ? !score.equals(result.score) : result.score != null) return false;
        if (duration != null ? !duration.equals(result.duration) : result.duration != null)
            return false;
        if (response != null ? !response.equals(result.response) : result.response != null)
            return false;
        return extensions != null ? extensions.equals(result.extensions) : result.extensions == null;
    }

    @Override
    public int hashCode() {
        int result = (completion ? 1 : 0);
        result = 31 * result + (success ? 1 : 0);
        result = 31 * result + (score != null ? score.hashCode() : 0);
        result = 31 * result + (duration != null ? duration.hashCode() : 0);
        result = 31 * result + (response != null ? response.hashCode() : 0);
        result = 31 * result + (extensions != null ? extensions.hashCode() : 0);
        return result;
    }
}
