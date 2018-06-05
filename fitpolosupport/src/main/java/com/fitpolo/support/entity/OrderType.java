package com.fitpolo.support.entity;

import java.io.Serializable;

public enum OrderType implements Serializable {
    NOTIFY("NOTIFY", "0000ffc2-0000-1000-8000-00805f9b34fb"),
    WRITE("WRITE", "0000ffc1-0000-1000-8000-00805f9b34fb"),
    ;


    private String uuid;
    private String name;

    OrderType(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }
}
