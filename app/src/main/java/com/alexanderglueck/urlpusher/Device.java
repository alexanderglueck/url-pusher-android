package com.alexanderglueck.urlpusher;

import com.google.gson.annotations.SerializedName;

public class Device {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("device_token")
    private String token;

    public Device(int id, String name, String token) {
        this.id = id;
        this.name = name;
        this.token = token;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
