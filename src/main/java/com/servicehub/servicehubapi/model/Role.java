package com.servicehub.servicehubapi.model;

public enum Role {
    ADMIN,
    PROVIDER,
    CLIENT;

    public String authority() {
        return "ROLE_" + name();
    }
}
