package com.ustadmobile.core.impl;

public class NoAppFoundException extends Exception {

    private String mimeType;

    public NoAppFoundException(String message, String mimeType) {
        super(message);
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
