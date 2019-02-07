package com.ustadmobile.lib.db.entities;

public class SELNominationItem {
    private String clazzName;
    private String questionSetTitle;
    private String nominatorName;
    private String nomineeName;
    private String questionText;
    private long nomineeUid;
    private long nominatorUid;
    private long clazzUid;
    private long selQuestionUid;


    public String getClazzName() {
        return clazzName;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }

    public String getQuestionSetTitle() {
        return questionSetTitle;
    }

    public void setQuestionSetTitle(String questionSetTitle) {
        this.questionSetTitle = questionSetTitle;
    }

    public String getNominatorName() {
        return nominatorName;
    }

    public void setNominatorName(String nominatorName) {
        this.nominatorName = nominatorName;
    }

    public String getNomineeName() {
        return nomineeName;
    }

    public void setNomineeName(String nomineeName) {
        this.nomineeName = nomineeName;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public long getNomineeUid() {
        return nomineeUid;
    }

    public void setNomineeUid(long nomineeUid) {
        this.nomineeUid = nomineeUid;
    }

    public long getNominatorUid() {
        return nominatorUid;
    }

    public void setNominatorUid(long nominatorUid) {
        this.nominatorUid = nominatorUid;
    }

    public long getClazzUid() {
        return clazzUid;
    }

    public void setClazzUid(long clazzUid) {
        this.clazzUid = clazzUid;
    }

    public long getSelQuestionUid() {
        return selQuestionUid;
    }

    public void setSelQuestionUid(long selQuestionUid) {
        this.selQuestionUid = selQuestionUid;
    }
}
