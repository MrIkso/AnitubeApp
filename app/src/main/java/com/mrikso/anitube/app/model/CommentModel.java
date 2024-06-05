package com.mrikso.anitube.app.model;

import com.google.common.base.Objects;

import java.io.Serializable;

public class CommentModel implements Serializable {
    private int commentId;
    private String username;
    private String userAvarar;
    private String userLink;
    private String userGroup;
    private String time;
    private String content;

    public int getCommentId() {
        return this.commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserAvarar() {
        return this.userAvarar;
    }

    public void setUserAvarar(String userAvarar) {
        this.userAvarar = userAvarar;
    }

    public String getUserLink() {
        return this.userLink;
    }

    public void setUserLink(String userLink) {
        this.userLink = userLink;
    }

    public String getUserGroup() {
        return this.userGroup;
    }

    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentModel that = (CommentModel) o;
        return commentId == that.commentId && Objects.equal(username, that.username) && Objects.equal(userAvarar, that.userAvarar) && Objects.equal(userLink, that.userLink) && Objects.equal(userGroup, that.userGroup) && Objects.equal(time, that.time) && Objects.equal(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(commentId, username, userAvarar, userLink, userGroup, time, content);
    }
}
