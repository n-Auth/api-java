package com.nauth.api;

/**
 * Created by pieter on 4/21/16.
 */
public enum RequestType {
    LOGIN("LOGIN"),
    REGISTER("ENROL");

    private String type;

    RequestType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
