package com.alexanderglueck.urlpusher;

public class User {
    String username;
    String fullName;
    String apiToken;
    int id;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getApiToken() {
        return apiToken;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }


}
