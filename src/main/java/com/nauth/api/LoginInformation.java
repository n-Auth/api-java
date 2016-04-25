package com.nauth.api;

/**
 * Created by pieter on 4/25/16.
 */
public class LoginInformation {

    private boolean loggedIn;
    private boolean canProvoke;
    private String userId;
    private String loginImage;
    private String publicKey;
    private String hashedSessionId;

    public LoginInformation(boolean loggedIn, boolean canProvoke, String userId, String loginImage, String publicKey, String hashedSessionId) {
        this.loggedIn = loggedIn;
        this.canProvoke = canProvoke;
        this.userId = userId;
        this.loginImage = loginImage;
        this.publicKey = publicKey;
        this.hashedSessionId = hashedSessionId;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean isCanProvoke() {
        return canProvoke;
    }

    public String getUserId() {
        return userId;
    }

    public String getLoginImage() {
        return loginImage;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getHashedSessionId() {
        return hashedSessionId;
    }
}
