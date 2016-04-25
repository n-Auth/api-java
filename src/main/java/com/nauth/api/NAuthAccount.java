package com.nauth.api;

import java.util.Date;

/**
 * Created by pieter on 4/25/16.
 */
public class NAuthAccount {

    private Long id;
    private boolean publicKeyAuthRevoked;
    private boolean publicKeyTransRevoked;
    private String description;
    private Date lastLogin;
    private Date creationDate;
    private boolean blocked;

    public NAuthAccount(Long id, boolean publicKeyAuthRevoked, boolean publicKeyTransRevoked, String description, Date lastLogin, Date creationDate, boolean blocked) {
        this.id = id;
        this.publicKeyAuthRevoked = publicKeyAuthRevoked;
        this.publicKeyTransRevoked = publicKeyTransRevoked;
        this.description = description;
        this.lastLogin = lastLogin;
        this.creationDate = creationDate;
        this.blocked = blocked;
    }

    public Long getId() {
        return id;
    }

    public boolean isPublicKeyAuthRevoked() {
        return publicKeyAuthRevoked;
    }

    public boolean isPublicKeyTransRevoked() {
        return publicKeyTransRevoked;
    }

    public String getDescription() {
        return description;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public boolean isBlocked() {
        return blocked;
    }
}
