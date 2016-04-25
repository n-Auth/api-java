package com.nauth.api;

/**
 * Created by pieter on 4/21/16.
 */
public enum ImageType {
    NAUTH("nauth"),
    QR("qr");

    private final String type;

    ImageType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
