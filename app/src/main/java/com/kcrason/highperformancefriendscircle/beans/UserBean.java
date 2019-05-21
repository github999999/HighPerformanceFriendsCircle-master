package com.kcrason.highperformancefriendscircle.beans;

public class UserBean {
    private String userAvatarUrl;  // 用户头像信息

    private String userName;  // 用户昵称

    private int userId;   // 用户id

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

}
