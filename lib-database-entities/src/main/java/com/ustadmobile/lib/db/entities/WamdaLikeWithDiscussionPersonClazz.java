package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class WamdaLikeWithDiscussionPersonClazz extends WamdaLike {

    private boolean discussionLiked;

    private int numLikes;

    private int numStudents;

    private int numShares;

    private boolean clazzLiked;

    @UmEmbedded
    private DiscussionPost discussionPost;

    @UmEmbedded
    private Person person;

    @UmEmbedded
    private Clazz clazz;

    public DiscussionPost getDiscussionPost() {
        return discussionPost;
    }

    public void setDiscussionPost(DiscussionPost discussionPost) {
        this.discussionPost = discussionPost;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Clazz getClazz() {
        return clazz;
    }

    public void setClazz(Clazz clazz) {
        this.clazz = clazz;
    }

    public boolean isDiscussionLiked() {
        return discussionLiked;
    }

    public void setDiscussionLiked(boolean discussionLiked) {
        this.discussionLiked = discussionLiked;
    }

    public int getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(int numLikes) {
        this.numLikes = numLikes;
    }

    public int getNumStudents() {
        return numStudents;
    }

    public void setNumStudents(int numStudents) {
        this.numStudents = numStudents;
    }

    public int getNumShares() {
        return numShares;
    }

    public void setNumShares(int numShares) {
        this.numShares = numShares;
    }

    public boolean isClazzLiked() {
        return clazzLiked;
    }

    public void setClazzLiked(boolean clazzLiked) {
        this.clazzLiked = clazzLiked;
    }
}
