package com.alexanderglueck.urlpusher.responses;

import com.google.gson.annotations.SerializedName;

public class SessionResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("api_token")
    private String apiToken;

    public SessionResponse(int id, String name, String email, String apiToken) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.apiToken = apiToken;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }
}
